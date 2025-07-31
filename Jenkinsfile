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
                                            # 调试输出
                                            echo "Current user: $(whoami)"
                                            echo "Searching ifconfig: $(which ifconfig)"
                                    
                                            # 安装依赖或使用绝对路径
                                            if ! command -v ifconfig &> /dev/null; then
                                                sudo yum install -y net-tools || echo "Install failed"
                                            fi
                                    
                                            # 验证网络
                                            /usr/sbin/ifconfig -a
                                    
                                            # 验证文件传输
                                            ls -l /etc/httpd/conf.d/*.conf
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

