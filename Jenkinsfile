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
                            configName: '192.168.88.84', // 你的SSH Server配置名称
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'fib.py',
                                    removePrefix: '',
                                    remoteDirectory: '/home/admin/app/',
                                    execCommand: '''
                                    chmod +x /home/admin/app/fib.py
                                    python3 /home/admin/app/fib.py
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

