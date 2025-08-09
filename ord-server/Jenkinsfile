pipeline {
    agent any
    environment {
        app = "ord-server"  // 替换为您的应用名称
    }
    stages {
        stage('Deploy JAR') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'node-84',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: "${app}/target/*.jar",
                                    removePrefix: "${app}/target/",
                                    remoteDirectory: "${app}",
                                    execCommand: """
                                        # 查找最新JAR文件
                                        start_jar=\$(find /home/admin/app/${app}/*.jar | xargs ls -td | grep ${app}.*.jar | head -n 1)
                                        
                                        # 备份当前JAR
                                        cp -a \$start_jar \$start_jar-\$(date +%Y%m%d%H%M%S)
                                        
                                        # 停止正在运行的服务
                                        NowPid=\$(ps -ef|grep -v grep|grep jar|grep ${app}|awk '{print \$2}')
                                        if [ -n "\$NowPid" ]; then
                                            kill -9 \$NowPid
                                        fi
                                        
                                        # 准备日志目录
                                        log_dir="/tmp/log/${app}"
                                        mkdir -p \$log_dir
                                        touch \$log_dir/${app}.log
                                        
                                        # 启动新服务
                                        nohup java -Xmx512m -Xms512m -Djava.security.egd=file:/dev/./urandom \\
                                                   -jar \$start_jar \\
                                                   --spring.profiles.active=uat \\
                                                   >> \$log_dir/${app}.log 2>&1 &
                                    """
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
}
