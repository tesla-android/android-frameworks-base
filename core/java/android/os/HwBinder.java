/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

import libcore.util.NativeAllocationRegistry;

import java.util.NoSuchElementException;

/** @hide */
public abstract class HwBinder implements IHwBinder {
    private static final String TAG = "HwBinder";

    private static final NativeAllocationRegistry sNativeRegistry;

    public HwBinder() {
        native_setup();

        sNativeRegistry.registerNativeAllocation(
                this,
                mNativeContext);
    }

    @Override
    public final native void transact(
            int code, HwParcel request, HwParcel reply, int flags)
        throws RemoteException;

    public abstract void onTransact(
            int code, HwParcel request, HwParcel reply, int flags)
        throws RemoteException;

    public native final void registerService(String serviceName)
        throws RemoteException;

    public static final IHwBinder getService(
            String iface,
            String serviceName)
        throws RemoteException, NoSuchElementException {
        return getService(iface, serviceName, false /* retry */);
    }
    public static native final IHwBinder getService(
            String iface,
            String serviceName,
            boolean retry)
        throws RemoteException, NoSuchElementException;

    public static native final void configureRpcThreadpool(
            long maxThreads, boolean callerWillJoin);

    public static native final void joinRpcThreadpool();

    /**
     * Call configureRpcThreadpool, then actually spawn
     * (maxThreads - (callerWillJoin ? 0 : 1)) threads.
     */
    public static final native void startRpcThreadPool(
            long maxThreads, boolean callerWillJoin);

    // Returns address of the "freeFunction".
    private static native final long native_init();

    private native final void native_setup();

    static {
        long freeFunction = native_init();

        sNativeRegistry = new NativeAllocationRegistry(
                HwBinder.class.getClassLoader(),
                freeFunction,
                128 /* size */);
    }

    private long mNativeContext;

    private static native void native_report_sysprop_change();

    /**
     * Notifies listeners that a system property has changed
     */
    public static void reportSyspropChanged() {
        native_report_sysprop_change();
    }
}
