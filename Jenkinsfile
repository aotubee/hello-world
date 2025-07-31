pipeline {
    agent any
    stages {
        stage('Deploy HTTPD') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'node-84',  // Jenkins 中配置的 SSH 服务器名称
                            transfers: [
                                sshTransfer(
                                    execCommand: '''
                                        sudo yum install -y httpd || true   # CentOS/RHEL
                                        sudo systemctl start httpd
                                        sudo systemctl enable httpd
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
