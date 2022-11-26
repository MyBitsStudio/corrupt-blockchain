package io.mybits.network.packet;

import io.mybits.network.Network;
import io.mybits.network.NetworkConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

public record PacketSecurity(Packet packet) {

    private static final Network network = Network.singleton();

    public PacketSecurity decode() {
        if (packet == null) return this;
        if (packet.invalid()) return this;
        packet.security = this;
        if (packet.getOpCode().getOpcode() == 1) {
            network.increment();
            return this;
        } else {
            switch (packet.getType()) {
                case FIXED, VARIABLE_BYTE, VARIABLE_SHORT, STRING_SHORT, STRING_LONG -> {
                    String decoded = decrypt(packet.getByteData()[0]);
                    if(decoded == null || decoded.equals("llun")) {
                        System.out.println("null decode");
                        packet.properties.add(PacketProperties.INVALID_DATA);
                    } else {
                        packet.setData(decoded.split(":"));
                    }
                    if(packet.data.length != packet.opCode.getLengths()[0]){
                        System.out.println("Invalid Length");
                        packet.properties.add(PacketProperties.INVALID_LENGTH);
                    }
                    if(verifyHeader(2)){
                        System.out.println("Error Header");
                        packet.properties.add(PacketProperties.INVALID_HEADER);
                    }
                    if(verifyFooter(packet.data.length - 1)){
                        System.out.println("Error Footer");
                        packet.properties.add(PacketProperties.INVALID_FOOTER);
                    }
                }
                case ARRAY_STRING -> {
                    packet.setData(decryptedArrayList(packet.listData).toArray(new String[0]));
                    if(packet.data.length != packet.opCode.getLengths()[0]){
                        System.out.println("Invalid Length");
                        packet.properties.add(PacketProperties.INVALID_LENGTH);
                    }
                    if(verifyHeader(1)){
                        System.out.println("Error Header");
                        packet.properties.add(PacketProperties.INVALID_HEADER);
                    }
                    if(verifyFooter(packet.data.length - 1)){
                        System.out.println("Error Footer");
                        packet.properties.add(PacketProperties.INVALID_FOOTER);
                    }
                }
            }
            if(packet.invalid()){
                System.out.println("Fatal Error");
                packet.properties.add(PacketProperties.FATAL_ERROR);
                return this;
            } else {
                network.increment();
            }
        }
        return this;
    }

    public static StringBuilder byteToString(byte[] a) {
        if (a == null) {
            return null;
        } else {
            StringBuilder ret = new StringBuilder();

            for (byte b : a) {
                if (b != 0) {
                    ret.append((char) b);
                }
            }

            return ret;
        }
    }

    public static @NotNull String reverseString(String packet) {
        return (new StringBuilder()).append(packet).reverse().toString();
    }

    public static @Nullable String encrypt(String content, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(1, publicKey);
            int splitLength = ((RSAPublicKey) publicKey).getModulus().bitLength() / 8 - 11;
            byte[][] arrays = splitBytes(content.getBytes(), splitLength);
            StringBuilder sb = new StringBuilder();

            for (byte[] array : arrays) {
                sb.append(bytesToHexString(cipher.doFinal(array)));
            }

            return sb.toString();
        } catch (Exception var10) {
            var10.printStackTrace();
            return null;
        }
    }

    public static @Nullable String decrypt(String content, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(2, privateKey);
            int splitLength = ((RSAPrivateKey) privateKey).getModulus().bitLength() / 8;
            byte[] contentBytes = hexString2Bytes(content);
            byte[][] arrays = splitBytes(contentBytes, splitLength);
            StringBuilder sb = new StringBuilder();

            for (byte[] array : arrays) {
                sb.append(new String(cipher.doFinal(array)));
            }

            return sb.toString();
        } catch (Exception var11) {
            return null;
        }
    }

    public @Nullable String decrypt(byte[] packet) {
        String first = reverseString(byteToString(packet).toString());
        String second;
        try {
            second = decrypt(first, NetworkConstants.privateKey);
        } catch (Exception e) {
            return null;
        }
        return reverseString(second);
    }

    public @NotNull String encrypt(String content){
        String first = reverseString(content);
        String second = encrypt(first, NetworkConstants.publicKey);
        return reverseString(second);
    }

    public static byte[] @NotNull [] splitBytes(byte @NotNull [] bytes, int splitLength) {

        int y = bytes.length % splitLength;
        int x;
        if (y == 0) {
            x = bytes.length / splitLength;
        } else {
            x = bytes.length / splitLength + 1;
        }

        byte[][] arrays = new byte[x][];

        for (int i = 0; i < x; ++i) {
            byte[] array;
            if (i == x - 1 && bytes.length % splitLength != 0) {
                array = new byte[bytes.length % splitLength];
                System.arraycopy(bytes, i * splitLength, array, 0, bytes.length % splitLength);
            } else {
                array = new byte[splitLength];
                System.arraycopy(bytes, i * splitLength, array, 0, splitLength);
            }

            arrays[i] = array;
        }

        return arrays;
    }

    public static @NotNull String bytesToHexString(byte @NotNull [] bytes) {

        StringBuilder sb = new StringBuilder(bytes.length);
        int var4 = bytes.length;

        for (byte aByte : bytes) {
            String sTemp = Integer.toHexString(255 & aByte);
            if (sTemp.length() < 2) {
                sb.append(0);
            }

            sb.append(sTemp.toUpperCase());
        }

        return sb.toString();
    }

    public static byte @NotNull [] hexString2Bytes(@NotNull String hex) {

        int len = hex.length() / 2;
        hex = hex.toUpperCase();
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();

        for (int i = 0; i < len; ++i) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }

        return result;
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public @NotNull List<String> decryptedArrayList(@NotNull List<String> info) {
        ArrayList<String> decrypted = new ArrayList<>();
        for(int i = 0; i < info.size(); i++){
            if(i == 0)
                continue;
            decrypted.add(decrypt(info.get(i).getBytes()));
        }
        return decrypted;
    }

    public @NotNull String header(){
        return network.getSerials(packet.channel).get(1) + ":" + network.radix() + ":" + network.getSerials(packet.channel).get(0);
    }

    public @NotNull String footer(){
        return network.getSerials(packet.channel).get(0) + ":" + network.radix() + ":" + network.getSerials(packet.channel).get(1);
    }

    public boolean verifyHeader(int index){
        return !header().equals(packet.data[index]);
    }

    public boolean verifyFooter(int index){
        return !footer().equals(packet.data[index]);
    }

}
