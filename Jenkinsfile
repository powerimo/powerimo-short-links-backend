pipeline {
    environment {
        DOCKER_REPO = "docker-repo.andewil.com"
        IMG_SERVER = "${DOCKER_REPO}/shortlinks/server"
        MAJOR_VERSION="1.0"
        BUILD_VERSION="${MAJOR_VERSION}.${BUILD_NUMBER}"
    }

    tools{
        maven 'maven3'
        jdk 'OpenJDK17'
    }

    agent any

    stages {
        stage('Initialization') {
            steps {
                sh 'java -version'
                sh 'mvn --version'
                sh 'echo HOME=${HOME}'
                sh 'echo PATH=${PATH}'
                sh 'echo M2_HOME=${M2_HOME}'
                sh 'echo BUILD_VERSION=${BUILD_VERSION}'
                sh 'echo IMG_SERVER=${IMG_SERVER}'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests=true clean compile versions:set -DnewVersion=${BUILD_VERSION}'
                sh 'mvn package'
            }
        }
        stage("Deploy artifacts to Nexus") {
            steps {
                sh 'mvn -DskipTests=true deploy'
            }
        }
        stage('Docker images') {
            when {
                branch 'master'
            }
            steps {
                sh 'echo ${IMG_SERVER}:$BUILD_VERSION'
                sh 'docker build -t ${IMG_SERVER}:$BUILD_VERSION -t ${IMG_SERVER}:${MAJOR_VERSION} ./powerimo-short-links-setver'
                sh 'docker push ${IMG_SERVER}:$BUILD_VERSION'
                sh 'docker push ${IMG_SERVER}:${MAJOR_VERSION} '
            }
        }
        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'deployagent', keyFileVariable: 'SSH_I', passphraseVariable: '', usernameVariable: 'SSH_USER_NAME')]) {
                    sh 'ssh -o StrictHostKeyChecking=no -p 40220 -i $SSH_I $SSH_USER_NAME@app.andewil.com ./deploy-short-links-server-prod.sh'
                }
            }
        }
    }

    post {
        success {
            sh '/var/jenkins_home/deployscripts/send-message.sh Jenkins "Job <b>$JOB_NAME</b> was completed and artifacts were deployed. Build version: <b>$BUILD_VERSION</b>. Author: $CHANGE_AUTHOR($CHANGE_AUTHOR_EMAIL)" '
        }
        failure {
            sh '/var/jenkins_home/deployscripts/send-message.sh Jenkins "Job <b>$JOB_NAME</b> failed. Build version: <b>$BUILD_VERSION</b> Author: $CHANGE_AUTHOR($CHANGE_AUTHOR_EMAIL)" '
        }
    }

}
