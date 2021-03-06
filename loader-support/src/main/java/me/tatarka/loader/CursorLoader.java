package me.tatarka.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContentResolverCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import me.tatarka.retainstate.RetainState;

/**
 * A {@link Loader} that queries a cursor and responds to changes much like {@link
 * android.content.CursorLoader}.
 */
public final class CursorLoader extends Loader<Cursor> {

    public static RetainState.OnCreate<CursorLoader> create(final Builder builder) {
        return new RetainState.OnCreate<CursorLoader>() {
            @Override
            public CursorLoader onCreate() {
                return builder.build();
            }
        };
    }

    private final ForceLoadContentObserver observer;

    private final ContentResolver resolver;
    private final Uri uri;
    private final String[] projection;
    private final String selection;
    private final String[] selectionArgs;
    private final String sortOrder;

    private Cursor cursor;
    private AsyncTask<Void, Void, Cursor> task;
    private CancellationSignal cancellationSignal;

    CursorLoader(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        observer = new ForceLoadContentObserver();
        this.resolver = resolver;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    @Override
    protected void onStart(final Receiver receiver) {
        task = new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                synchronized (this) {
                    if (isCancelled()) {
                        return null;
                    }
                    cancellationSignal = new CancellationSignal();
                }

                try {
                    Cursor cursor = ContentResolverCompat.query(resolver,
                            uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
                    if (cursor != null) {
                        try {
                            // Ensure the cursor window is filled.
                            cursor.getCount();
                            cursor.registerContentObserver(observer);
                        } catch (RuntimeException e) {
                            cursor.close();
                            throw e;
                        }
                    }
                    return cursor;
                } catch (OperationCanceledException e) {
                    // The query was canceled, this result won't be delivered anyway.
                    return null;
                } finally {
                    synchronized (this) {
                        cancellationSignal = null;
                    }
                }
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                if (!isRunning()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }

                Cursor oldCursor = CursorLoader.this.cursor;
                CursorLoader.this.cursor = cursor;

                receiver.deliverResult(cursor);

                if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
                    oldCursor.close();
                }
            }
        };
        task.execute();
    }

    @Override
    protected void onCancel() {
        task = null;
        synchronized (this) {
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private final class ForceLoadContentObserver extends ContentObserver {
        ForceLoadContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            restart();
        }
    }

    public static class Builder {
        private final ContentResolver resolver;
        private final Uri uri;
        private String[] projection;
        private String selection;
        private String[] selectionArgs;
        private String sortOrder;

        public Builder(ContentResolver resolver, Uri uri) {
            this.resolver = resolver;
            this.uri = uri;
        }

        public Builder projection(String... projection) {
            this.projection = projection;
            return this;
        }

        public Builder selection(String selection, String... selectionArgs) {
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public CursorLoader build() {
            return new CursorLoader(resolver, uri, projection, selection, selectionArgs, sortOrder);
        }
    }
}
