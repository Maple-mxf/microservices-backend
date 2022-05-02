#!/bin/bash

# 自定义gradle缓存镜像(原生docker构建需要此基础镜像加速构建)
cache_image_name="gradle-cache:latest"

# 远程构建的git仓库上下文
gitRepo="https://github.com/Maple-mxf/microservices-backend"

# 需要构建的模块
module="$1"

# 构建方式，可选有 docker|buildpakcks|jib|buildkit
buildType="$2"

# 构建的镜像版本
version=$(date +'%Y%m%d%H%M%S')

# 远程镜像仓库
remoteCR="ccr.ccs.tencentyun.com/tcb-2446011668-egvs/"

echo "module is $module"

if [ -z "$module" ];then
    echo "module args require"
    exit 0
fi
if [ -z "$buildType" ];then
    echo "default buildType is docker"
    buildType="docker"
fi

# docker images filter cmd https://docs.docker.com/engine/reference/commandline/images/
if [[ -n $(docker images -q --filter=reference=$cache_image_name) ]];then
    echo "Found gradle cache images"
else
  echo "Not Found gradle cache images. process docker build"
  docker build \
  -f Dockerfile.cache \
  -t ${cache_image_name} ${gitRepo}
fi

# 构建镜像
timer_start=$(date "+%Y-%m-%d %H:%M:%S")

case $buildType in
"docker")
docker build \
--no-cache  \
--build-arg MODULE="${module}" \
--build-arg CACHE_IMAGE="${cache_image_name}" \
--tag "${module}":"${version}" ${gitRepo} \
&& docker image prune -f
;;

"buildpacks")
pack build --path=./ --builder heroku/buildpacks:20 "${module}":"${version}"
;;

"buildkit")
docker build \
--no-cache  \
--build-arg MODULE="${module}" \
--build-arg CACHE_IMAGE="${cache_image_name}" \
--file Dockerfile.buildkit \
--tag "${module}":"${version}" ${gitRepo}
;;

"jib")
gradle :"${module}":jib --image=$remoteCR"${module}":"${version}"
;;
esac

timer_end=$(date "+%Y-%m-%d %H:%M:%S")
duration=$(echo $(($(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}"))) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}')

echo "Build success, buildType=$buildType,costTime=$duration"