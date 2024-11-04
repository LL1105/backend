#!/bin/bash

# 定义远程服务器信息
REMOTE_USER="ubuntu"
REMOTE_HOST="49.232.232.203"
REMOTE_PORT="22"
REMOTE_DIR="/gitgle"
LOG_DIR="/gitgle/gitgle.logs" # 日志目录

# 本地JAR包路径
declare -A JAR_PATHS=(
    ["user-service"]="user-service/user-service-server/target/user-service.jar"
    ["data-service"]="data-service/data-service-server/target/data-service.jar"
    ["domain-service"]="domain-service/domain-service-server/target/domain-service.jar"
    ["nation-service"]="nation-service/nation-service-server/target/nation-service.jar"
    ["talentrank-service"]="talentrank-service/talentrank-service-server/target/talentrank-service.jar"
    ["api-service"]="api-service/target/api-service.jar"
    ["gateway-service"]="gateway-service/target/gateway-service.jar"
)

# 检查传入的参数
if [ "$#" -eq 0 ]; then
    echo "请指定要上传的服务名称，例如: ./script.sh user-service 或 ./script.sh all"
    exit 1
fi

# 获取服务名称
SERVICE_NAME="$1"

# 执行mvn clean install
echo "执行mvn clean install..."
mvn clean install

if [ $? -ne 0 ]; then
    echo "mvn命令执行失败，脚本终止。"
    exit 1
fi

# 上传服务
if [ "$SERVICE_NAME" == "all" ]; then
    # 上传所有服务
    for jar_name in "${!JAR_PATHS[@]}"; do
        JAR_PATH=${JAR_PATHS[$jar_name]}
        echo "传输 $jar_name 到远程服务器..."
        scp -P "$REMOTE_PORT" "$JAR_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/"
    done
else
    # 上传指定服务
    if [[ ! -v JAR_PATHS["$SERVICE_NAME"] ]]; then
        echo "无效的服务名称: $SERVICE_NAME"
        echo "可用的服务: ${!JAR_PATHS[@]}"
        exit 1
    fi

    JAR_PATH=${JAR_PATHS[$SERVICE_NAME]}
    JAR_NAME=$(basename "$JAR_PATH")
    echo "传输 $JAR_NAME 到远程服务器..."
    scp -P "$REMOTE_PORT" "$JAR_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/$JAR_NAME"
fi

if [ $? -ne 0 ]; then
    echo "传输失败，脚本终止。"
    exit 1
fi

echo "在远程服务器上停止现有的服务..."
ssh -p "$REMOTE_PORT" "$REMOTE_USER@$REMOTE_HOST" << EOF
cd "$REMOTE_DIR" || exit

# 检查并创建日志目录
if [ ! -d "$LOG_DIR" ]; then
    echo "创建日志目录 $LOG_DIR..."
    mkdir -p "$LOG_DIR"
fi

# 查找并停止指定的服务
if [ "$SERVICE_NAME" == "all" ]; then
    for jar in *.jar; do
        for pid in \$(pgrep -f "\$jar"); do
            echo "停止 PID \$pid..."
            kill -9 \$pid
        done
    done
else
    for pid in \$(pgrep -f "$SERVICE_NAME"); do
        echo "停止 PID \$pid..."
        kill -9 \$pid
    done
fi

# 确保所有 JAR 包已停止
sleep 2

echo "在远程服务器上启动新 JAR 包..."

# 启动 data-service 首先
if [ "$SERVICE_NAME" == "all" ]; then
    echo "启动 data-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "data-service.jar" > "$LOG_DIR/data-service.jar.log" 2>&1 &

    # 等待 data-service 启动完成
    sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 talentrank-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "talentrank-service.jar" > "$LOG_DIR/talentrank-service.jar.log" 2>&1 &
    sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 nation-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "nation-service.jar" > "$LOG_DIR/nation-service.jar.log" 2>&1 &
    sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 user-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "user-service.jar" > "$LOG_DIR/user-service.jar.log" 2>&1 &
    sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 domain-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "domain-service.jar" > "$LOG_DIR/domain-service.jar.log" 2>&1 &
    sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 api-service..."
        nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "api-service.jar" > "$LOG_DIR/api-service.jar.log" 2>&1 &
        sleep 10 # 这里可以根据需要调整等待时间

    echo "启动 gateway-service..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "gateway-service.jar" > "$LOG_DIR/gateway-service.jar.log" 2>&1 &
    sleep 10 # 这里可以根据需要调整等待时间

#    for jar in *.jar; do
#        if [ "\$jar" != "data-service.jar" ]; then
#            echo "启动 \$jar..."
#            nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "\$jar" > "$LOG_DIR/\$jar.log" 2>&1 &
#        fi
#    done
else
    echo "启动 \$SERVICE_NAME..."
    nohup java -DDUBBO_IP_TO_REGISTRY="49.232.232.203" -jar "$JAR_NAME" > "$LOG_DIR/\$SERVICE_NAME.log" 2>&1 &
fi
EOF

echo "所有操作完成。"
