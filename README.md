# 🚀 Maven-Jenkins-CICD-Docker-Chatbot-Project

A robust CI/CD pipeline implementation for deploying a chatbot application using Maven, Jenkins, and Docker. This project demonstrates modern DevOps practices with automated building, testing, and deployment workflows.

## Features
Automated build pipeline using Jenkins
Containerized deployment with Docker
Maven-based Java project structure
Continuous Integration and Continuous Deployment
Chatbot application with RESTful endpoints
Automated testing suite
Docker image optimization

## 🛠️ Tech Stack
Java 17
SpringAI
Vaadin
Maven
Jenkins
Docker
Git

## Project Architecture

![Architecture Diagram](https://github.com/omoluabidotcom/maven-jenkins-cicd-docker-chatbot-project-/blob/main/Images/Architecture%20Image.png)

### Objective:

1. Deploy your Java (Maven) web app from GitHub to an EC2 server using Jenkins and Docker. You’ll push code → Jenkins builds WAR → Docker builds image → Runs app.

This guide walks you through **deploying a Java Web App** using:
- ✅ Maven (to build)
- ✅ Docker (to containerize and run)
- ✅ Jenkins (to automate everything)
- ✅ EC2 (to host Jenkins)

---

## 🧱 PART 1: Setup Jenkins EC2 Server

### 🔧 STEP 1: Launch EC2 in AWS
1.	Go to AWS Console → EC2 → Launch Instance
2.	Instance Name: jenkins-server
3.	OS: Choose Ubuntu 20.04
4.	Instance Type: t2.medium (2GB RAM minimum for Docker + Jenkins)
5.	Key Pair: Create/download a new key pair (save .pem file)
6.	Network Settings:
      Open the ports below:
      -  ✅ SSH (22)
      -	 ✅ HTTP (80)
      -  ✅ Custom TCP → Port 8080 (for Jenkins)
7.	Click Launch Instance

➡️ Done? Copy the **Public IPv4** address, you’ll need it.


---

### 🖥️ STEP 2: Connect to EC2

```bash
ssh -i your-key.pem ubuntu@<your-ec2-ip>
```

---

### ⚙️ STEP 3: Install Java, Maven, Git, Docker

```bash
sudo apt update -y
sudo apt install maven git -y
sudo apt install -y openjdk-21-jdk
git --version
mvn -v
java -version
```

✅ Install Docker:

```bash
# Add Docker's official GPG key:
sudo apt update
sudo apt install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Signed-By: /etc/apt/keyrings/docker.asc
EOF

#Install the Docker packages.
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Enable and test Docker
sudo systemctl enable docker
sudo systemctl start docker
sudo docker run hello-world
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo systemctl status docker

```

🔄 If Docker Is Not Running
1. If it shows inactive or failed, you can try restarting and checking logs:
   ```bash
   sudo systemctl restart docker
   sudo journalctl -u docker --no-pager --lines=30

   ```


---

### 🤖 STEP 4: Install Jenkins

```bash

sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins
```

✅ Confirm Jenkins is running:
```bash
sudo systemctl status jenkins
```
✅ Give Jenkins Docker access:

```bash
sudo usermod -aG docker jenkins
id jenkins


#Restart the instance (or logout and login again):
sudo reboot
```

✅ Open Jenkins in browser:
```
http://<your-ec2-ip>:8080
```

```bash

#Get the first-time password:
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

- Paste password → Install Suggested Plugins → Create Admin

---

## 🚚 PART 2: Prepare GitHub Repo

### 🗂️ Your Project Structure (✅ you already shared)

Make sure your GitHub project contains:

- `Dockerfile` (your final one)
- `pom.xml` (for the Chatbot web app)
- `.gitignore` (exclude `target/`)

### 🆙 Push Code to GitHub (if not already)

```bash
git init
git remote add origin https://github.com/<your-username>/<your-repo>.git
git add .
git commit -m "commit message here"
git push -u origin main
```

---

## 🛠️ PART 3: Create Jenkins CI/CD Pipeline

### 🔐 STEP 6: Add GitHub Credentials to Jenkins

1. Go to Jenkins → Manage Jenkins → Credentials → (Global) → Add Credentials
2. Choose:
    - Kind: **Username & Password** OR **GitHub PAT**
    - ID: `github-creds`

---

### ⚙️ STEP 7: Create a Jenkins Pipeline Job

1. Go to Jenkins Dashboard → **New Item**
2. Name: `java-webapp-cicd`
3. Choose: **Pipeline** → Click **OK**

In the "Pipeline" section → choose **“Pipeline Script”** and paste this:

```groovy

pipeline {
  agent any
  environment {
    IMAGE_NAME = 'springai-3.5.4'
  }
  stages {
      stage('Clone') {
          steps {
              git branch: 'main',
                      credentialsId: 'github-creds',
                      url: 'https://github.com/omoluabidotcom/maven-jenkins-cicd-docker-chatbot-project-.git'
          }
      }
    stage('Maven Build') {
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker build -t $IMAGE_NAME .'
      }
    }
    stage('Run Container') {
      steps {
        sh '''
        docker rm -f java-webapp-container || true
        docker run -d -p 80:8080 --name java-webapp-container $IMAGE_NAME
        '''
      }
    }
  }
}


```

---


---

## 🚀 PART 4: Run Your Jenkins Job

### ▶️ STEP 8: Start the Pipeline

1. Click **Build Now**
2. Console Output shows:
    - Git Clone ✅
    - Maven Build WAR ✅
    - Docker Image ✅
    - Docker Container ✅

---

### 🌍 STEP 9: Open Web App

In browser:
```
http://<your-ec2-ip:8080/chat>
```


🎉 Voila your Chatbot is live from the WAR file running inside Tomcat + Docker!

---

## 📦 PART 5: Bonus Commands

```bash
# List containers
docker ps

# # All containers (including stopped)
docker ps -a 

# Stop container
docker stop java-webapp-container

# Remove container
docker rm java-webapp-container

# Remove image
docker rmi java-webapp

#🔁 Rebuild the Docker Image
docker build -t java-webapp-container .

#🚀 Run the Container (exposing port 8080)
docker run -d --name java-webapp-container -p 8080:8080 java-webapp

#📂 See Container Logs
docker logs java-webapp-container

# 📥 Copy Files From a Running Container
docker cp java-webapp-container:/opt/tomcat/webapps/springai-3.5.4.war ./springai-3.5.4.war

#🖥️ Access Container Shell
docker exec -it java-webapp-container bash

#💥 Force Remove All Stopped Containers and Dangling Images
docker container prune -f
docker image prune -f

#📋 List Docker Images
docker images

```

---

## 📊 Architecture Diagram (Text View)

```
[GitHub] 
   |
   | (Push code)
   v
[Jenkins EC2 Server]
   |--> Maven Build WAR
   |--> Docker Build Image
   |--> Docker Run Container
   |
   v
[Your Web App] → http://<EC2-IP:8080/chat>
```

---

### ✅ Diagram to Understand (Kid-style)

```
+---------------------+      GitHub Push       +---------------------+
|   Developer Laptop  |  ------------------->  |     GitHub Repo     |
+---------------------+                        +---------------------+
                                                    |
                                                    v
      +------------------------------------------------------------+
      | EC2 Ubuntu Server (Jenkins + Docker + Java + Maven)        |
      |                                                            |
      |  [ Jenkins Pipeline ]                                      |
      |   1. Clone Code from GitHub                                |
      |   2. Build WAR using Maven                                 |
      |   3. Build Docker image using Dockerfile                   |
      |   4. Run Container exposing port 80                        |
      +------------------------------------------------------------+
                                                    |
                                                    v
                                 Web App available at http://<EC2-IP:80/chat>
```

---

## ✅ What We Achieved

| ✅ Task | Done |
|--------|------|
| EC2 created with right ports | ✅ |
| Jenkins installed and running | ✅ |
| Docker, Maven, Java setup | ✅ |
| Jenkins pipeline for your code | ✅ |
| WAR built and deployed inside Docker | ✅ |
| Web app live on EC2 | ✅ |

---
## 🚀 PART 4: Build Artifacts Location (Workspace)
## ✅ 1. Build Artifacts Location (Workspace)

Every time Jenkins runs a job, it stores all the files in the workspace.

### ✅ Option A: GUI – Jenkins Dashboard

1. Go to your Jenkins job.
2. Click on the last **successful build**.
3. Click on **"Workspace"** (left sidebar).
4. You'll see:
    - Your project folder
    - Compiled files
    - `target/` folder → Contains `.war` file:
      ```
      /var/lib/jenkins/workspace/java-webapp-cicd/target/springai-3.5.4.war
      ```

### ✅ Option B: Jenkins Server (Linux Terminal)

```bash
cd /var/lib/jenkins/workspace/<your-job-name>/
ls -l

cd target/
ls -l
# springai-3.5.4.war should be here
```

---

### ✅ 2. If You Used archiveArtifacts in Pipeline

If your Jenkinsfile has:

```groovy
archiveArtifacts artifacts: 'target/*.war', fingerprint: true
```

Then Jenkins archives the WAR file under:

> Jenkins → Job → Build → **"Archived Artifacts"**

---

### ✅ 3. If You Built Docker Image

1. If Jenkins builds a Docker image, the WAR gets copied into the Tomcat image in the Docker layer, but not stored locally on Jenkins after build (unless you keep a copy manually).

```bash
docker exec -it <container_id> bash
cd /opt/tomcat/webapps/
ls -l
# You’ll see: springai-3.5.4.war and expanded webapp/
```


---

## ✅ Summary

| Case | Where to Look                                                |
|------|--------------------------------------------------------------|
| Maven .war build | `/var/lib/jenkins/workspace/<job>/target/springai-3.5.4.war` |
| GUI – Workspace | Jenkins → Job → Build → Workspace                            |
| Archived Artifacts | Jenkins → Job → Build → Archived Artifacts                   |
| Docker container WAR file | `/opt/tomcat/webapps/` inside container                      |

---

## 💣 Project Destroy Option (When You Don't Need It Anymore)

### ✅ Updated Jenkinsfile with Destroy Option

```groovy
pipeline {
  agent any
  environment {
    IMAGE_NAME = 'java-webapp'
    CONTAINER_NAME = 'java-webapp-container'
  }
	
  parameters {
    booleanParam(name: 'DESTROY', defaultValue: false, description: 'Check this if you want to destroy everything (container, image, workspace)')
  }

  stages {
    stage('Clone') {
      when { expression { !params.DESTROY } }
      steps {
        git branch: 'main', credentialsId: 'github-creds', url: 'https://github.com/omoluabidotcom/maven-jenkins-cicd-docker-chatbot-project-.git'
      }
    }

    stage('Maven Build') {
      when { expression { !params.DESTROY } }
      steps {
        sh 'mvn clean install springai-3.5.4'
      }
    }

    stage('Build Docker Image') {
      when { expression { !params.DESTROY } }
      steps {
        sh 'docker build -t $IMAGE_NAME .'
      }
    }

    stage('Run Container') {
      when { expression { !params.DESTROY } }
      steps {
        sh '''
          docker rm -f $CONTAINER_NAME || true
          docker run -d -p 80:8080 --name $CONTAINER_NAME $IMAGE_NAME
        '''
      }
    }

    stage('Destroy Everything') {
      when { expression { params.DESTROY } }
      steps {
        echo "🧨 Destroying container, image, and cleaning workspace..."

        sh '''
          echo "Stopping and removing container..."
          docker rm -f $CONTAINER_NAME || true

          echo "Removing Docker image..."
          docker rmi -f $IMAGE_NAME || true

          echo "Cleaning Jenkins workspace..."
          rm -rf * || true
        '''
      }
    }
  }

  post {
    always {
      echo "✅ Pipeline completed."
    }
  }
}

```

---