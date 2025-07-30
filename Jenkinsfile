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
        stage('安装Apache HTTP服务') {  
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
                                            # 安装httpd并启动
                                            sudo yum install -y httpd
                                            sudo systemctl enable --now httpd
                                            
                                            # 验证安装
                                            echo "Apache版本：$(httpd -v)"
                                            curl -I 127.0.0.1:80 | grep '200 OK'
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

