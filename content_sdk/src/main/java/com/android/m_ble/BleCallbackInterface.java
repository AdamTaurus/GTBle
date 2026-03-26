/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.m_ble;
public interface BleCallbackInterface extends android.os.IInterface
{
  /** Default implementation for BleCallbackInterface. */
  public static class Default implements BleCallbackInterface
  {
    /** 收到控制消息 */
    @Override public void onCtrlReceive(String message) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements BleCallbackInterface
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.m_ble.BleCallbackInterface interface,
     * generating a proxy if needed.
     */
    public static BleCallbackInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof BleCallbackInterface))) {
        return ((BleCallbackInterface)iin);
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
        case TRANSACTION_onCtrlReceive:
        {
          String _arg0;
          _arg0 = data.readString();
          this.onCtrlReceive(_arg0);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements BleCallbackInterface
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
      /** 收到控制消息 */
      @Override public void onCtrlReceive(String message) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(message);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onCtrlReceive, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onCtrlReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final String DESCRIPTOR = "com.android.m_ble.BleCallbackInterface";
  /** 收到控制消息 */
  public void onCtrlReceive(String message) throws android.os.RemoteException;
}
