pipeline {
    agent any
    
    stages {
        stage('Pre-Check') {
            steps {
                echo "🚀 Starting deployment to node-84"
                echo "Current workspace: ${env.WORKSPACE}"
            }
        }
        
        stage('Test SSH Connection') {
            steps {
                script {
                    echo "🔍 Testing SSH connection to node-84..."
                    
                    // 使用try-catch捕获错误并提供更友好的错误信息
                    try {
                        sshPublisher(
                            publishers: [
                                sshPublisherDesc(
                                    configName: 'node-84',
                                    transfers: [
                                        sshTransfer(
                                            execCommand: '''
                                                echo "✅ SSH Connection Successful"
                                                echo "🖥️ Hostname: $(hostname)"
                                                echo "💻 Kernel Info: $(uname -a)"
                                                echo "📂 Creating test file: /tmp/test.txt"
                                                touch /tmp/test.txt
                                                ls -l /tmp/test.txt
                                                echo "🟢 All commands executed successfully"
                                            '''
                                        )
                                    ]
                                )
                            ]
                        )
                    } catch (Exception e) {
                        echo "❌ SSH Connection Failed: ${e.getMessage()}"
                        // 添加详细错误日志
                        def sshLog = findFiles(glob: '**/publish-over-ssh@*/log') 
                        if (sshLog) {
                            echo "📄 SSH Error Log:"
                            sh "cat ${sshLog[0].path}"
                        }
                        error("SSH connection test failed")
                    }
                }
            }
        }
        
        stage('Verify Connection') {
            steps {
                echo "🔬 Verifying test file creation..."
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'node-84',
                            transfers: [
                                sshTransfer(
                                    execCommand: '''
                                        echo "🔎 Checking test file:"
                                        if [ -f /tmp/test.txt ]; then
                                            echo "✅ Test file exists: /tmp/test.txt"
                                            echo "🗂️ File contents:"
                                            cat /tmp/test.txt
                                        else
                                            echo "❌ Test file not found!"
                                            exit 1
                                        fi
                                    '''
                                )
                            ]
                        )
                    ]
                )
            }
        }
        
        stage('Post-Check') {
            steps {
                echo "🏁 SSH connection test completed successfully"
                echo "Remote host is ready for deployment"
            }
        }
    }
    
    post {
        success {
            echo "🎉 All stages completed successfully!"
            slackSend channel: '#deployments', message: "SSH connection to node-84 verified successfully. Build: ${env.BUILD_URL}"
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
            emailext subject: "Pipeline Failed: ${env.JOB_NAME}",
                     body: "SSH connection test failed for node-84. Build: ${env.BUILD_URL}",
                     to: 'dev-ops@example.com'
        }
        always {
            echo "🧹 Cleaning up workspace..."
            cleanWs()
        }
    }
}
