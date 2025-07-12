pipeline {
    agent any  // 在任何可用节点执行（[[0]](#__0) [[16]](#__16)）
    stages {
        stage('拉取代码') {
            steps {
                checkout scm  // 自动同步 SCM 配置的仓库（[[2]](#__2) [[16]](#__16)）
            }
        }
        stage('执行斐波那契脚本') {
            steps {
                sh 'python3 fib.py'  // 执行仓库中的脚本（[[10]](#__10) [[17]](#__17)）
            }
        }
    }
}

