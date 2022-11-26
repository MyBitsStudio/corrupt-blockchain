package io.mybits.protect;

import io.mybits.utils.Utilities;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class Serials {

    public static  SecureRandom random = new SecureRandom();

    @Contract(pure = true)
    public static @NotNull String serial(){
        return recollect(transit(Utilities.createRandomString(12)));
    }

    @Contract(pure = true)
    public static @NotNull String superSerial(String serial){
        return recollect(disengage(recollect(transit(serial))));
    }

    public static @NotNull String transit(@NotNull String transmitted){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < transmitted.length() * 12; i++){
            if(i % 2 == 0){
                int randoms = random.nextInt(3);
                for(int j = 0; j < randoms; j++){
                    sb.append((char)random.nextInt(34, 122));
                }
            } else {
                int randoms = random.nextInt(6);
                for(int j = 0; j < randoms; j++){
                    sb.append((char)random.nextInt(34, 122));
                }
            }
        }
        return sb.substring(sb.length() / 2).replace(":", "z");
    }

    public static @NotNull String recollect(@NotNull String transit){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < transit.length(); i++){
            if(i % 2 == 0){
                sb.append(transit.charAt(i));
            }
        }
        return sb.toString().replace(":", "z");
    }

    public static @NotNull String disengage(@NotNull String recollected){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < recollected.length() * 3; i++){
            if(i % 2 != 0){
                for(int j = 0; j < 3; j ++)
                    sb.append((char)random.nextInt(34, 122));
            }
        }
        return sb.toString().replace(":", "z");
    }
}
