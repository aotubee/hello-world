pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git url: 'git@github.com:aotubee/hello-world.git', 
                    credentialsId: 'a242ab64-b9d4-496f-a120-610abdd9dcb1',
                    branch: 'main'
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

