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
                                    execCommand: '''
                                        set -e  # 错误时立即退出（[[11]](#__11)）
                                        echo "正在创建测试文件..."
                                        sudo touch /tmp/test.txt && sudo chmod 644 /tmp/test.txt
                                        echo "文件详情："
                                        ls -lh /tmp/test.txt
                                        echo "删除测试文件..."
                                        sudo rm -f /tmp/test.txt
                                    '''
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
    post {  // 新增结果处理模块（[[12]](#__12) [[20]](#__20)）
        always {
            echo "===== 执行结束时间：${new Date()} ====="
            archiveArtifacts artifacts: '**/ssh.log', allowEmptyArchive: true
        }
        failure {
            emailext body: 'SSH部署失败，请检查日志', subject: 'Pipeline Failed'
        }
    }
}

