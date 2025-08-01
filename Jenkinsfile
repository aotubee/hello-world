pipeline {
    agent any
    options {
        timeout(time: 10, unit: 'MINUTES')  // 添加超时控制（[[18]](#__18)）
    }
    stages {
        stage('环境准备') {  // 新增前置检查阶段
            steps {
                script {
                    echo "===== 开始执行时间：${new Date()} ====="
                    echo "当前工作目录：${env.WORKSPACE}"
                }
            }
        }
        
        stage('测试SSH基础连接') {  // 分解为独立验证阶段
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'node-84',
                            verbose: true,  // 开启详细日志（[[20]](#__20)）
                            transfers: [
                                sshTransfer(
                                    execCommand: '''
                                        echo "### 连接基本信息 ###"
                                        echo "用户：$(whoami)"
                                        echo "主机名：$(hostname)"
                                        echo "内核版本：$(uname -a)"
                                    '''
                                )
                            ]
                        )
                    ]
                )
            }
        }

        stage('验证文件操作') {  // 新增文件操作验证阶段
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'node-84',
                            transfers: [
                                sshTransfer(
                                    execCommand: ' echo "20250801" > /tmp/test.txt '
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
}

