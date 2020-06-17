package com.oneliang.ktx.frame.test.mail

import com.oneliang.ktx.frame.mail.Mail
import com.oneliang.ktx.frame.mail.SendMailInformation
import com.oneliang.ktx.frame.mail.ToAddress

fun main() {
    val mailBean = SendMailInformation()
    mailBean.fromAddress = "stephen8558@163.com"
    mailBean.host = "smtp.163.com"
    mailBean.user = "stephen8558"
    mailBean.password = "GHIZBBBUIYZWDPDX"
    mailBean.subject = "test"
    mailBean.content = "test"
    mailBean.addToAddress(ToAddress("582199098@qq.com"))
    Mail.send(mailBean)
    //receive
//		ReceiveMailInformation receiveMailInformation=new ReceiveMailInformation();
//		receiveMailInformation.setHost("mail.kidgrow.cn");
//		receiveMailInformation.setUser("noreply@kidgrow.cn");
//		receiveMailInformation.setPassword("t6g4f3");
//		List<MailMessage> mailMessageList=Mail.receive(receiveMailInformation);
//		String path="C:\\temp";
//		FileUtil.createDirectory(path);
//		for(MailMessage mailMessage:mailMessageList){
//			System.out.println(mailMessage.getFromAddress());
//			mailMessage.saveAccessories(path);
//		}
}