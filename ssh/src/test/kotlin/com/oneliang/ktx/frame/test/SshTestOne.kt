package com.oneliang.ktx.frame.test

import com.jcraft.jsch.Channel
import java.io.File
import javax.swing.JOptionPane


fun main() {
    val ssh = Ssh()
    val host = "122.51.110.167"
    val user = "root"
    val password = JOptionPane.showInputDialog("Enter password")
    val file = File("/D:/settings.zip")
    val begin = System.currentTimeMillis()
    ssh.connect(host, user = user, password = password, configMap = mapOf("userauth.gssapi-with-mic" to "no", "StrictHostKeyChecking" to "no"), afterSessionConnect = { session ->
        val channel: Channel = session.openChannel("shell")
        channel.inputStream = System.`in`
        channel.outputStream = System.out
        channel.connect()
//        println("cost:" + (System.currentTimeMillis() - begin))
//        Thread.sleep(5000)
//        channel.disconnect()
//        println("shell disconnect")
//        val execChannel = session.openChannel("exec")
//        (execChannel as ChannelExec).setCommand("ls")
//        execChannel.setInputStream(null)
//
//        execChannel.setErrStream(System.err)
//        execChannel.connect()
//        val inputStream: InputStream = execChannel.getInputStream()
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        inputStream.copyTo(byteArrayOutputStream)
//        println(String(byteArrayOutputStream.toByteArray()))
//        execChannel.disconnect()
//        val directory = "/home/test"
//        if (channel is ChannelSftp) {
//            try { //执行列表展示ls 命令
//                channel.ls(directory) //执行盘符切换cd 命令
//                channel.cd(directory)
//                val bufferedInputStream = BufferedInputStream(FileInputStream(file))
//                channel.put(bufferedInputStream, file.name)
//            } catch (t: Throwable) {
//                t.printStackTrace()
//                //mkdir when ls/cd exception
//                channel.mkdir(directory)
//            }
//        }
    })
}