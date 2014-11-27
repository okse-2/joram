#!/bin/bash

export EC2_URL=http://127.0.0.1:80/services/Cloud
export EC2_ACCESS_KEY=admin
export EC2_SECRET_KEY=password

SERVICE_TOKEN=999888777666

JORSERV=10.0.0.2
JORDIR=/root/joram-factory
DISK=$JORDIR/disk.img
KERNEL=$JORDIR/kernel.img
IMAGE_NAME=joram-vm
KERNEL_NAME=joram-kernel

mount -o loop $DISK $JORDIR/mnt
rm -rf $JORDIR/mnt/root/*
cp -r $JORDIR/pack/* $JORDIR/mnt/root
umount $JORDIR/mnt

glance delete -f -A $SERVICE_TOKEN $(glance index -A $SERVICE_TOKEN | grep $IMAGE_NAME | cut -f1 -d' ')
glance delete -f -A $SERVICE_TOKEN $(glance index -A $SERVICE_TOKEN | grep $KERNEL_NAME | cut -f1 -d' ')

RVAL=`glance add -A $SERVICE_TOKEN name=$KERNEL_NAME is_public=true container_format=aki disk_format=aki < $KERNEL`
KERNEL_ID=`echo $RVAL | cut -d":" -f2 | tr -d " "`
glance add -A $SERVICE_TOKEN name=$IMAGE_NAME is_public=true container_format=ami disk_format=ami kernel_id=$KERNEL_ID < $DISK

NEWAMI=$(euca-describe-images | grep $IMAGE_NAME | cut -f2)
echo "EC2 ID: $NEWAMI"

ssh $JORSERV sed -i "s/IMAGE_ID/$NEWAMI/g" /root/joram/config/elasticity.properties
