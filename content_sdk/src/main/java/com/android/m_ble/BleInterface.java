/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.m_ble;
public interface BleInterface extends android.os.IInterface
{
  /** Default implementation for BleInterface. */
  public static class Default implements BleInterface
  {
    /** 发送控制消息（JSON/String） */
    @Override public void sendCtrl(String pkg, String message) throws android.os.RemoteException
    {
    }
    /** 注册回调；多次调用可覆盖 */
    @Override public void registerReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException
    {
    }
    /** 解除回调注册 */
    @Override public void unregisterReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements BleInterface
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.m_ble.BleInterface interface,
     * generating a proxy if needed.
     */
    public static BleInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof BleInterface))) {
        return ((BleInterface)iin);
      }
      return new Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_sendCtrl:
        {
          String _arg0;
          _arg0 = data.readString();
          String _arg1;
          _arg1 = data.readString();
          this.sendCtrl(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerReceive:
        {
          String _arg0;
          _arg0 = data.readString();
          BleCallbackInterface _arg1;
          _arg1 = BleCallbackInterface.Stub.asInterface(data.readStrongBinder());
          this.registerReceive(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterReceive:
        {
          String _arg0;
          _arg0 = data.readString();
          BleCallbackInterface _arg1;
          _arg1 = BleCallbackInterface.Stub.asInterface(data.readStrongBinder());
          this.unregisterReceive(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements BleInterface
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** 发送控制消息（JSON/String） */
      @Override public void sendCtrl(String pkg, String message) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(pkg);
          _data.writeString(message);
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendCtrl, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** 注册回调；多次调用可覆盖 */
      @Override public void registerReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(pkg);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerReceive, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** 解除回调注册 */
      @Override public void unregisterReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(pkg);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterReceive, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_sendCtrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_registerReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_unregisterReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  public static final String DESCRIPTOR = "com.android.m_ble.BleInterface";
  /** 发送控制消息（JSON/String） */
  public void sendCtrl(String pkg, String message) throws android.os.RemoteException;
  /** 注册回调；多次调用可覆盖 */
  public void registerReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException;
  /** 解除回调注册 */
  public void unregisterReceive(String pkg, BleCallbackInterface callback) throws android.os.RemoteException;
}
