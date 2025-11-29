# === Stage 1: Build Java WAR using Maven ===
FROM ubuntu:20.04 AS build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y curl gnupg2 ca-certificates \
  && curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
  && apt-get install -y openjdk-21-jdk maven nodejs \
  && rm -rf /var/lib/apt/lists/* \

#RUN apt-get update && apt-get install -y \
#    openjdk-21-jdk \
#    maven \
#    wget \
#    curl \
#    git \
#    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /src

COPY . .
RUN mvn clean package -Pproduction

# Run the Maven build in production mode so Vaadin generates flow-build-info.json
RUN mvn -DskipTests -Dvaadin.productionMode=true -Pproduction package

# === Stage 2: Runtime with Tomcat and Manager GUI ===
FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH
ENV CATALINA_OUT=/proc/self/fd/1
ENV CATALINA_ERR=/proc/self/fd/2

# Tomcat version
#ENV TOMCAT_VERSION=9.0.95
ENV TOMCAT_VERSION=10.1.24
ENV CATALINA_HOME=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

# Download and setup Tomcat
RUN wget https://archive.apache.org/dist/tomcat/tomcat-10/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
    mkdir -p $CATALINA_HOME && \
    tar xzvf /tmp/tomcat.tar.gz -C $CATALINA_HOME --strip-components=1 && \
    rm /tmp/tomcat.tar.gz

#RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
#    mkdir -p $CATALINA_HOME && \
#    tar xzvf /tmp/tomcat.tar.gz -C $CATALINA_HOME --strip-components=1 && \
#    rm /tmp/tomcat.tar.gz

# ✅ Setup Tomcat users for GUI access
RUN echo '<tomcat-users>' > $CATALINA_HOME/conf/tomcat-users.xml && \
    echo '  <role rolename="manager-gui"/>' >> $CATALINA_HOME/conf/tomcat-users.xml && \
    echo '  <role rolename="admin-gui"/>' >> $CATALINA_HOME/conf/tomcat-users.xml && \
    echo '  <role rolename="manager-script"/>' >> $CATALINA_HOME/conf/tomcat-users.xml && \
    echo '  <user username="admin" password="admin" roles="manager-gui,admin-gui,manager-script"/>' >> $CATALINA_HOME/conf/tomcat-users.xml && \
    echo '</tomcat-users>' >> $CATALINA_HOME/conf/tomcat-users.xml

# ✅ Remove IP restrictions in context.xml files
RUN sed -i '/<Valve className="org.apache.catalina.valves.RemoteAddrValve"/d' $CATALINA_HOME/webapps/manager/META-INF/context.xml || true
RUN sed -i '/<Valve className="org.apache.catalina.valves.RemoteAddrValve"/d' $CATALINA_HOME/webapps/host-manager/META-INF/context.xml || true

# Expose port
EXPOSE 8080

# Forward ALL logs to Docker stdout/stderr (including application logs)
RUN ln -sf /proc/self/fd/1 $CATALINA_HOME/logs/catalina.out
RUN ln -sf /proc/self/fd/1 $CATALINA_HOME/logs/catalina.log
RUN ln -sf /proc/self/fd/1 $CATALINA_HOME/logs/localhost.log
RUN ln -sf /proc/self/fd/2 $CATALINA_HOME/logs/localhost_access_log.txt

# ✅ Deploy WAR
COPY --from=build /src/target/springai-3.5.4.war $CATALINA_HOME/webapps/springai-3.5.4.war

# Start Tomcat with output redirected to Docker logs
CMD ["sh", "-c", "catalina.sh run 2>&1"]

