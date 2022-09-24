# BurpUnicodeTransfer

平时在测试过程中可能会遇到响应为unicode的时候，还得去在线转换，太麻烦了

这款工具可以自动转换proxy以及repeater消息中的unicode

## 效果

![image-20220924192612415.png](https://github.com/xunyang1/BurpUnicodeTransfer/blob/main/images/image-20220924192612415.png)

未开启时

![image-20220924192639662.png](https://github.com/xunyang1/BurpUnicodeTransfer/blob/main/images/image-20220924192639662.png)

开启时

repeater模块

![image-20220924192657654.png](https://github.com/xunyang1/BurpUnicodeTransfer/blob/main/images/image-20220924192657654.png)

proxy模块

![image-20220924192729143.png](https://github.com/xunyang1/BurpUnicodeTransfer/blob/main/images/image-20220924192729143.png)

PS: 起初有中文乱码问题，调试了半天发现是this.helpers.StringtoBytes的问题，该函数好像不是进行utf8编码，直接用str.getbytes就好了qwq
