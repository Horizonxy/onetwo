package org.onetwo.common.encrypt;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.utils.LangUtils;

public class MDFactoryTest {
	
	/****
	 * 把原始的密码字符串用utf8编码成byte数组，加8位随机字节，然后用sha算法散列。
把散列结果+加上之前的8位随机字节后，在用base64编码。即：
base64( sha(to_byte_array(source)+salt) + salt )
salt为8个随机字节。
	 */
	@Test
	public void testMD(){
		String source = "123456";
		String encrypt = MDFactory.getMDEncrypt().encryptWithSalt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		boolean rs = MDFactory.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
		
		encrypt = MDFactory.getMDEncrypt().encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		rs = MDFactory.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);

		MDEncrypt sha = new MDEncryptImpl("SHA", 20, false);
		encrypt = sha.encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		
		rs = sha.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
		


		MDEncrypt sha1 = MDFactory.create("sha", 20, false, true);
		encrypt = sha1.encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		
		rs = sha1.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
	}
	
	@Test
	public void testCsrf(){
		String source = "77352BFB8E1D171486DE6E4AC7CBE53D";
		MDEncrypt md5 = MDFactory.MD5;
		
		String encrypt = md5.encryptWithSalt(source);
		LangUtils.println("testMDWithoutLabel after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		boolean rs = md5.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
		
		
		encrypt = "36D69B4433C402F087A666A004B6A8ECD9BCD663C68F3700";
		LangUtils.println("testMDWithoutLabel after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		rs = md5.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
	}
	@Test
	public void testMDWithoutLabel(){
		String source = "12356";
		String encrypt = MDFactory.createMD5(false, false).encrypt(source);
		LangUtils.println("testMDWithoutLabel after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		encrypt = MDFactory.createMD5(false, false).encryptWithSalt(source);
		LangUtils.println("testMDWithoutLabel after encryptWithSalt : ${0}, ${1}", encrypt.length(), encrypt);
		
		boolean rs = MDFactory.createMD5(false, true).checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
		
		encrypt = MDFactory.getMDEncrypt("SMD5").encryptWithSalt(source);
		LangUtils.println("smd5 after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
	}
	

	@Test
	public void testSha(){
		String source = "123456";
		String encrypt = MDFactory.getMDEncrypt("SHA").encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		boolean rs = MDFactory.getMDEncrypt("SHA").checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
		
		encrypt = MDFactory.getMDEncrypt("SHA").encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);

		MDEncrypt sha = new MDEncryptImpl("SHA", 20, false);
		encrypt = sha.encrypt(source);
		LangUtils.println("after encrypt : ${0}, ${1}", encrypt.length(), encrypt);
		
		rs = sha.checkEncrypt(source, encrypt);
		Assert.assertEquals(true, rs);
	}


	@Test
	public void testSign(){
		String DEFAULT_SSO_SIGN_KEY = "asdfa7sd9fa[ko@#$s0df]pips9";
		String source = LangUtils.appendNotBlank("6RmyQ2 t1KFIIvAijFE9wqPFMXNwfSXMiKDvEg==", DEFAULT_SSO_SIGN_KEY);
		String entrystr = MDFactory.MD5.encryptWithSalt(source);
//		boolean rs = MDFactory.MD5.checkEncrypt(source, "4B4AB0CF1EB5DD19D8B7C24F0D0626F20085AD9EA9D03D02");
		boolean rs = MDFactory.MD5.checkEncrypt(source, entrystr);
		Assert.assertEquals(true, rs);
	}

	@Test
	public void generate(){
		String source = "test";
		String salt = "test";
		String entrystr = MDFactory.MD5.encryptWithSalt(source, salt);
		System.out.println("generate: " + entrystr);
//		boolean rs = MDFactory.MD5.checkEncrypt(source, "4B4AB0CF1EB5DD19D8B7C24F0D0626F20085AD9EA9D03D02");
		boolean rs = MDFactory.MD5.checkEncrypt(source, salt, entrystr);
		Assert.assertEquals(true, rs);
	}

}
