pipeline {
    agent any  // 在所有可用节点执行
    stages {
        stage('拉取代码') {
            steps {
                checkout scm  // 自动同步 SCM 配置的仓库
            }
        }
        stage('示例任务') {
            steps {
                sh 'echo "Hello World!"'  // 输出测试信息
            }

        stage('执行fib.py') {
            steps {
                sh 'python3 ./fib.py'  // 输出测试信息
            }

        stage('deploy nginx') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: "node-84",
                            transfers: [
                                sshTransfer (
                                    execCommand: """
                                    docker run -d --name nginx-server \\
                                      -p 80:80 \\
                                      -p 443:443 \\
                                      --restart always \\
                                      nginx:alpine
                                    
                                    # 验证容器状态
                                    sleep 5
                                    docker ps -f name=nginx-server
                                    """
                                    )
                                ]
                            )
                        ]
                    )
                }
            }

        }
    }
}
