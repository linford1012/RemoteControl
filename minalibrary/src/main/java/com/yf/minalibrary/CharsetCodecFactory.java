package com.yf.minalibrary;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Created by wuhuai on 2016/10/20 .
 * ;
 */

public class CharsetCodecFactory implements ProtocolCodecFactory {


    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return new CharsetDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return new CharsetEncoder();
    }
}