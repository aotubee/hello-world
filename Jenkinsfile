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
                sh 'pwd' 
                sh 'python3 ./fib.py' 
            }
        }
        stage('远程部署') {
            steps {
                script {
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "node-84",
                                transfers: [
                                    sshTransfer (
                                        execCommand: '''
                                            sh 'python3 /tmp/fib.py' 
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

