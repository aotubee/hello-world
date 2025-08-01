pipeline {
    agent any
    stages {
		stage('Test SSH Connection') {
			steps {
				sshPublisher(
					publishers: [
						sshPublisherDesc(
							configName: 'node-84',
							transfers: [
								sshTransfer(
									execCommand: 'echo "SSH Connection Successful"; hostname; uname -a;touch /tmp/test.txt'
								)
							]
						)
					]
				)
			}
		}
    }
}
