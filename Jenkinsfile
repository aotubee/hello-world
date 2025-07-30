pipeline {
    agent any
    stages {
        stage('拉取代码') {
            steps { checkout scm }
        }
        stage('示例任务') {
            steps { 
                sh 'echo "Hello World!"' 
            }
        }
        stage('执行fib.py') {
            steps { 
                sh 'python3 ./fib.py' 
            }
        }
        stage('部署Nginx') {
            steps {
                script {
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "node-84",
                                transfers: [
                                    sshTransfer (
                                        execCommand: '''
                                            docker run -d --name nginx-server \
                                                -p 80:80 \
                                                -p 443:443 \
                                                --restart always \
                                                nginx:alpine
                                            sleep 5
                                            docker ps -f name=nginx-server
                                        '''
                                    )
                                ],
                                usePromotionTimestamp: false,
                                useWorkspaceInPromotion: false
                            )
                        ]
                    )
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}

