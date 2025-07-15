pipeline {
    agent any
    stages {
        stage('拉取代码') {
            steps {
                checkout scm  // 自动同步 SCM 配置的仓库（[[2]](#__2) [[16]](#__16)）
            }
        }

        stage('Deploy') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: '192.168.88.84',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'fib.py',
                                    remoteDirectory: '/tmp/',
                                    execCommand: '''
                                        chmod +x /tmp/fib.py
                                        python3 /tmp/fib.py
                                    '''
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
}

