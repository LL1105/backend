#!/bin/bash

# 定义远程服务器信息
REMOTE_USER="ubuntu"
REMOTE_HOST="49.232.232.203"
REMOTE_PORT="22" # 默认为22，如果使用默认端口可以不设置
REMOTE_DIR="/gitgle" # 远程服务器上的目录

# 本地JAR包路径
JAR_PATHS=(
    "user-service/user-service-server/target/user-service.jar"
    "data-service/data-service-server/target/data-service.jar"
    "domain-service/domain-service-server/target/domain-service.jar"
    "talentrank-service/talentrank-service-server/target/talentrank-service.jar"
    "api-service/target/api-service.jar"
    "gateway-service/target/gateway-service.jar"
    # 添加更多服务的JAR包路径
)

# 执行mvn clean install
echo "执行mvn clean install..."
mvn clean install

# 检查mvn命令是否执行成功
if [ $? -ne 0 ]; then
    echo "mvn命令执行失败，脚本终止。"
    exit 1
fi

# 使用SCP传输JAR包到远程服务器
echo "传输JAR包到远程服务器..."
for jar_path in "${JAR_PATHS[@]}"; do
    # 获取JAR包名称
    jar_name=$(basename $jar_path)
    echo "传输 $jar_name 到远程服务器..."
    scp -P $REMOTE_PORT $jar_path $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/$jar_name
done

# 检查SCP命令是否执行成功
if [ $? -ne 0 ]; then
    echo "SCP命令执行失败，脚本终止。"
    exit 1
fi

# 使用SSH在远程服务器上启动JAR包
echo "在远程服务器上启动JAR包..."
ssh -p $REMOTE_PORT $REMOTE_USER@$REMOTE_HOST << 'EOF'
cd $REMOTE_DIR
for jar in *.jar; do
    echo "启动 $jar..."
    nohup java -jar $jar >/dev/null 2>&1 &
done
EOF

echo "所有操作完成。"