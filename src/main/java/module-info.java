module io.mybits.hyperledger {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.jetbrains.annotations;
    requires org.bouncycastle.provider;
    requires com.google.common;
    requires jeromq;
    requires io.netty.transport;
    requires io.netty.codec;

    opens io.mybits.hyperledger to javafx.fxml;
    exports io.mybits.hyperledger;
}