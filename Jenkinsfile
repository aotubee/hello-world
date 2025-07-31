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
        stage('ssh deploy') {  
            steps {
                script {
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: "node-84",
                                transfers: [
                                    sshTransfer (
                                        // 添加文件传输示例（按需配置）
                                        sourceFiles: "**/*.conf",  // 可传输配置文件
                                        remoteDirectory: "/etc/httpd/conf.d/",
                                        execCommand: '''
                                            ifconfig
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
        failure {  // 添加失败通知
            emailext body: '构建失败，请检查日志', subject: 'Pipeline Failed'
        }
    }
}

