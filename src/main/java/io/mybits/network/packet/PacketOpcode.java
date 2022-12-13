package io.mybits.network.packet;

import org.jetbrains.annotations.Nullable;

public enum PacketOpcode {

    VERIFY_REQUEST(new int[]{3}),
    TCP_VERIFY(new int[]{}),
    UDP_VERIFY(new int[]{}),
    REQUEST_TCP_VERIFIED(new int[]{0}),
    REQUEST_UDP_VERIFIED(new int[]{0}),
    REQUEST_TCP_DENIED(new int[]{0}),
    REQUEST_UDP_DENIED(new int[]{0}),

    WALLET_CREATE(new int[]{4}),
    WALLET_RESPONSE( new int[]{3}),
    WALLET_FETCH(new int[]{6}),
    WALLET_CALL(new int[]{6}),
    WALLET_ERROR(new int[]{6}),

    TRANSACTION_REQUEST(new int[]{4}),
    TRANSACTION_RESPONSE(new int[]{3}),
    TRANSACTION_ERROR(new int[]{3}),

    DENIED_PACKET(new int[]{0}),

    ;

    private final int[] LENGTHS;


    PacketOpcode(int[] length){
        this.LENGTHS = length;
    }

    public int getOpcode(){
        return this.ordinal();
    }

    public int[] getLengths(){
        return LENGTHS;
    }

    public static @Nullable PacketOpcode getOpcode(long opcode){
        for(PacketOpcode op : PacketOpcode.values()){
            if(op.getOpcode() == opcode){
                return op;
            }
        }
        return null;
    }
}
