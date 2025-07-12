pipeline {
    agent any  // 在所有可用节点执行（[[2]](#__2) [[15]](#__15)）
    stages {
        stage('拉取代码') {
            steps {
                checkout scm  // 自动同步 SCM 配置的仓库（[[14]](#__14) [[16]](#__16)）
            }
        }
        stage('示例任务') {
            steps {
                sh 'echo "Hello World!"'  // 输出测试信息（[[1]](#__1) [[10]](#__10)）
            }
        stage('执行fib.py') {
            steps {
                sh 'python3 ./fib.py'  // 输出测试信息（[[1]](#__1) [[10]](#__10)）
            }
        }
    }
}

