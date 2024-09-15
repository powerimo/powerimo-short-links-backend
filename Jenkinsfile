pipeline {
    environment {
        // Docker repository
        DOCKER_REPO = "${env.DOCKER_REPO_PREFIX}"

        // Server image name
        IMAGE_NAME = "${DOCKER_REPO}/powerimo/short-links-server"

        // Full branch name e.g. PWR-105
        FULL_PATH_BRANCH = "${env.BRANCH_NAME}"

        // Extracted version from the branch (e.g. '1.0.0' from the branch 'release/1.0.0')
        BRANCH_VERSION = "${env.BRANCH_NAME}".tokenize('/').last()

        // Commit code
        GIT_COMMIT_SHORT = "${env.GIT_COMMIT.take(7)}"

        // Notifications API Key
        NSS_API_KEY = credentials('powerimo-nss-api-key')

        // Deployment host parameters
        SSH_PORT = "${SSH_DEFAULT_PORT}"
        HOST_PROD = "${POWERIMO_HOST_PROD}"
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
                sh 'echo IMAGE_NAME=${IMAGE_NAME}'
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
                sh 'echo skipped'
                // sh 'mvn -DskipTests=true deploy'
            }
        }
        stage('Docker images') {
            when {
                anyOf {
                    branch 'qa'
                    branch pattern: "release/.*", comparator: "REGEXP"
                }
            }
            steps {
                def tag = "${env.BRANCH_NAME == 'qa' ? 'qa' : env.BRANCH_VERSION}-${GIT_COMMIT_SHORT}"
                def tag2 = "${env.BRANCH_NAME == 'qa' ? 'qa' : env.BRANCH_VERSION}"
                def dockerImage = "$IMAGE_NAME:$tag"
                def dockerImage2 = "$IMAGE_NAME:$tag2"
                sh "docker build -t ${dockerImage} -t ${dockerImage2} ."
                sh "docker push ${dockerImage}"
                sh "docker push ${dockerImage2}"
            }
        }
        stage('Deploy') {
            when {
                anyOf {
                    branch pattern: "release/.*", comparator: "REGEXP"
                }
            }
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'deployagent', keyFileVariable: 'SSH_I', passphraseVariable: '', usernameVariable: 'SSH_USER_NAME')]) {
                    sh 'scp -o StrictHostKeyChecking=no -P $SSH_PORT -i $SSH_I cicd/scripts/deploy.sh $SSH_USER_NAME@${HOST_PROD}:~/bin/deploy-powerimo-short-links-server-prod.sh'
                    sh 'ssh -o StrictHostKeyChecking=no -p $SSH_PORT -i $SSH_I $SSH_USER_NAME@${HOST_PROD} "chmod +x ~/bin/deploy-powerimo-short-links-server-prod.sh"'
                    sh 'ssh -o StrictHostKeyChecking=no -p $SSH_PORT -i $SSH_I $SSH_USER_NAME@${HOST_PROD} ./deploy-short-links-server-prod.sh'
                }
            }
        }
    }

    post {
        always {
            nssSendJobResult(recipients: "AndewilEventsChannel")
        }
    }

}
