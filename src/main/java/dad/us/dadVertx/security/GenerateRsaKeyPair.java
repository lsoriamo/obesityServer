package dad.us.dadVertx.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FilenameUtils;

import com.google.firebase.internal.Base64;

import io.vertx.core.impl.StringEscapeUtils;


public class GenerateRsaKeyPair {
	public static String privateKeyString = "A7372A497D606FD27341E117F0A5F615";
	public static String publicKeyString = "A7372A497D606FD27341E117F0A5F615";

	public static void generateSecurityKeys() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024); // key length
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			String privateKeyString = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);
			String publicKeyString = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);

			System.out.println("rsa key pair generated\n");
			System.out.println("privateKey\n" + privateKeyString + "\n");
			System.out.println("publicKey\n" + publicKeyString + "\n\n");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String encryptMsg(String message) {
		try {
			IvParameterSpec iv = new IvParameterSpec("7895123456789123".getBytes("UTF-8"));
			SecretKey secret = new SecretKeySpec(publicKeyString.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			byte[] cipherText = Base64.encode(cipher.doFinal(message.getBytes("UTF-8")), Base64.NO_WRAP);
			String strNewBody = StringEscapeUtils.escapeJava(new String(cipherText, "UTF-8"));
			return strNewBody;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File encryptFile(File file) {
		byte[] bFile = new byte[(int) file.length()];
		try {
			// convert file into array of bytes
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			bufferedInputStream.read(bFile);
			bufferedInputStream.close();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			// outputStream.write(decPortocol);
			outputStream.write(bFile);

			byte[] cryptedFileBytes = outputStream.toByteArray();
			// Cipher and encrypting
			IvParameterSpec iv = new IvParameterSpec("7895123456789123".getBytes("UTF-8"));
			SecretKey secret = new SecretKeySpec(publicKeyString.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			byte[] cipherText = cipher.doFinal(cryptedFileBytes);

			// Write Encrypted File
			File encrypted = new File(file.getPath() + "_encrypted." + FilenameUtils.getExtension(file.getPath()));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(encrypted, false));
			bufferedOutputStream.write(cipherText);
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			return encrypted;
		} catch (Exception e) {
			return null;
		}
	}
	
	

	public static String decryptMsg(String cipherText) {
		try {
			IvParameterSpec iv = new IvParameterSpec("7895123456789123".getBytes("UTF-8"));
			SecretKey secret = new SecretKeySpec(publicKeyString.getBytes("UTF-8"), "AES");
			Cipher cipher = null;
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			byte[] original = cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP));
			String strNewBody = StringEscapeUtils.unescapeJava(new String(original, "UTF-8"));
			return strNewBody;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static File decryptFile(File file, String pathTarget) {
		byte[] bFile = new byte[(int) file.length()];
		try {
			// convert file into array of bytes
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			bufferedInputStream.read(bFile);
			bufferedInputStream.close();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			// outputStream.write(decPortocol);
			outputStream.write(bFile);

			byte[] cryptedFileBytes = outputStream.toByteArray();
			// Cipher and encrypting
			IvParameterSpec iv = new IvParameterSpec("7895123456789123".getBytes("UTF-8"));
			SecretKey secret = new SecretKeySpec(publicKeyString.getBytes("UTF-8"), "AES");
			Cipher cipher = null;
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			byte[] cipherText = cipher.doFinal(cryptedFileBytes);

			// Write Encrypted File
			File encrypted = new File(pathTarget);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(encrypted, false));
			bufferedOutputStream.write(cipherText);
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			return encrypted;
		} catch (Exception e) {
			return null;
		}
	}
}