/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

/**
 *
 * @author mflohr
 */
import android.app.AlertDialog;
import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.Date;
import org.redPanda.ListMessage.Mes;
import org.redPandaLib.core.Test;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.ImageMsg;
import org.redPandaLib.core.messages.TextMsg;

/**
 *
 */
public class ChatAdapter extends BaseAdapter {

    final static int imageMaxSize = 400;
    private Context mContext;
    public ArrayList<ChatMsg> mMessages;
    private Bitmap placeholderBitmap;

    public ChatAdapter(Context context, ArrayList<ChatMsg> messages) {
        super();
        this.mContext = context;
        this.mMessages = messages;
        placeholderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_top);
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMsg cM = (ChatMsg) this.getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatrow, parent, false);
            holder.big = (LinearLayout) convertView.findViewById(R.id.chatrow);
            holder.bubbleLayout = (RelativeLayout) convertView.findViewById(R.id.bubble);
//            holder.head = (TextView) convertView.findViewById(R.id.head);
            holder.bubbleHead = (TextView) convertView.findViewById(R.id.bubbleHead);
            holder.bubbleText = (TextView) convertView.findViewById(R.id.bubbleText);
            holder.bubbleTime = (TextView) convertView.findViewById(R.id.bubbleTime);
            holder.bubbleDeliverd = (TextView) convertView.findViewById(R.id.bubbleDeliverd);
            holder.bubbleImage = null;
            //    holder.im = (ImageView) convertView.findViewById(R.id.thereic);
            convertView.setTag(holder);
            // holder.bubble.setPadding(0, 0, 0, 0);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }
        //   Toast.makeText(mContext, "blablabla", Toast.LENGTH_SHORT).show();
        // Mes mes = (Mes) b.text.get(0);
        String bub = "";

        String time = cM.getTime();
        final String content = cM.getText();
        String readText = cM.getDeliverdTo();

        //readText += " -";
//        if (!readText.equals("")) {
//            
//            readText = "<br><small>" + readText + "</small>";
//            
//        }
        //inAdapter iA = new inAdapter(mContext, mMessages.get(position).text);
        // bub += "<small>" + time + "</small> " + content + readText;
        //holder.bubbleText.setText(Html.fromHtml(bub));
        if (cM.getMsgType() == TextMsg.BYTE) {
            holder.bubbleText.setVisibility(View.VISIBLE);
            holder.bubbleText.setText(content);
            if (holder.bubbleImage != null) {
                holder.bubbleImage.get().setVisibility(View.GONE);
                holder.bubbleImage = null;
            }
        } else if (cM.getMsgType() == ImageMsg.BYTE) {
            holder.bubbleImage = null;
            if (holder.bubbleImage == null) {
                holder.bubbleImage = new WeakReference<ImageView>((ImageView) convertView.findViewById(R.id.bubbleImage));

            }

            // holder.bubbleImage.get().setImageBitmap(decodeFile(content, 200));
            loadBitmap(content, holder.bubbleImage.get(), imageMaxSize);
            holder.bubbleImage.get().setVisibility(View.VISIBLE);
            holder.bubbleText.setVisibility(View.GONE);

            holder.bubbleImage.get().setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + content), "image/*");
                    mContext.startActivity(intent);

                }
            });

        } else {
            holder.bubbleText.setVisibility(View.VISIBLE);
            holder.bubbleText.setText("MsgType not implemented");
            if (holder.bubbleImage != null) {
                holder.bubbleImage.get().setVisibility(View.GONE);
                holder.bubbleImage = null;
            }
        }

        if (!readText.equals("")) {
            holder.bubbleDeliverd.setText(readText);
            holder.bubbleDeliverd.setVisibility(View.VISIBLE);
            if (holder.bubbleImage != null) {
                holder.bubbleImage.get().setPadding(0, 0, 0, 40);
            }
        } else {
            holder.bubbleDeliverd.setVisibility(View.GONE);
            if (holder.bubbleImage != null) {
                holder.bubbleImage.get().setPadding(0, 0, 0, 0);
            }
        }
        holder.bubbleTime.setText(time);
        //System.out.println("1234 "+message.getData().getString("msg"));       
//        holder.message.setText(genReadableText(b));
        boolean fromMe = cM.isFromMe();

        LayoutParams lp;

//check if it is a status message then remove background, and change text color.
//        if (message.isStatusMessage()) {
//            holder.message.setBackgroundDrawable(null);
//            lp.gravity = Gravity.LEFT;
//
//        } else {
//Check whether message is mine to show green background and align to right
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (fromMe) {
            // params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            //  holder.bubble.setGravity(Gravity.RIGHT);
            //lp = (LayoutParams) holder.bubble.getLayoutParams();
            holder.bubbleHead.setText("");
            holder.bubbleLayout.setBackgroundResource(R.drawable.ich);
            // System.out.println(" ich");
            holder.big.setGravity(Gravity.RIGHT);

            //lp.gravity = Gravity.RIGHT;
//            holder.im.setVisibility(View.VISIBLE);
//            holder.im.getLayoutParams().width = 30;
//            holder.im.getLayoutParams().height = 30;
        } //If not mine then it is from sender to show orange background and align to left
        else {
            //  params.addRule(RelativeLayout.RIGHT_OF, R.id.bubbleHead);
            // holder.bubble.setGravity(Gravity.LEFT);
            //  lp = (LayoutParams) holder.bubble.getLayoutParams();
            holder.bubbleHead.setText(cM.getName());
            //params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            //            String strhex = Long.toHexString(cM.getIdentity()).toUpperCase();
            //            strhex = strhex.substring(strhex.length()-6, strhex.length());
            //            holder.bubbleHead.setTextColor(Color.parseColor("#"+strhex));
//            RelativeLayout.LayoutParams asd = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//           asd.addRule(RelativeLayout.RIGHT_OF,R.id.bubbleHead);
//           holder.bubbleLayout.setLayoutParams(asd);

            holder.bubbleHead.setTextColor(cM.getColor());
            //holder.bubbleHead.setText(Test.localSettings.identity2Name.get(b.identity));
            //            holder.im.setVisibility(View.INVISIBLE);
            //            holder.im.getLayoutParams().width = 0;
            //            holder.im.getLayoutParams().height = 0;
            holder.bubbleLayout.setBackgroundResource(R.drawable.du);
            // System.out.println(" du");
            holder.big.setGravity(Gravity.LEFT);
            //lp.gravity = Gravity.LEFT;

        }
        //Math.min(lp.width, (int) (getWidestView(mContext, iA)*1.05));
        //holder.bubbleLayout.setLayoutParams(params);
//            holder.message.setTextColor(R.color.textColor);
        // System.out.println("123456 " + b.text.size());
        // holder.bubble.getLayoutParams().height = (int) (getHeight(mContext, iA)+20);

        holder.bubbleHead.setOnLongClickListener(new BubbleHeadOnClickListener(cM, this));

        holder.bubbleText.setOnLongClickListener(new BubbleOnClickListener(cM));
        return convertView;
    }

    private static class ViewHolder {

        WeakReference< ImageView> bubbleImage;
        TextView bubbleHead;
        TextView bubbleText, bubbleTime, bubbleDeliverd;
        RelativeLayout bubbleLayout;
        LinearLayout big;
    }

    @Override
    public long getItemId(int position) {
//Unimplemented, because we aren't using Sqlite.
        return 0;
    }

    public void loadBitmap(String path, ImageView imageView, int reqSize) {
        if (cancelPotentialWork(path, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, path, reqSize);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), placeholderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute();
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private int reqSize = 0;
        private String path;

        public BitmapWorkerTask(ImageView imageView, String path, int reqSize) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.path = path;
            this.reqSize = reqSize;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {

            return decodeFile(path, reqSize);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (isCancelled()) {
                bitmap = null;

            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);

                }
            }
        }
    }

    public static boolean cancelPotentialWork(String path, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapPath = bitmapWorkerTask.path;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapPath.equals("") || !bitmapPath.equals(path)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {

        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                BitmapWorkerTask bitmapWorkerTask) {

            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private Bitmap decodeFile(String str, int REQUIRED_SIZE) {

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, o);

        //The new size we want to scale to
        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeFile(str, o2);

    }

    public static String genReadableText(Mes msg) {
        long sendTime = msg.ts;
        String str = msg.getMes();

        if (msg.message_type == DeliveredMsg.BYTE) {
            //str = "delivered...";
            return "";
        }

        Date date = new Date(sendTime);

        String out = "";

        out += formatTime(date) + ": " + str;

        return out;
    }

    public static String formatTime(Date date) {

        String hours = "" + date.getHours();
        String minutes = "" + date.getMinutes();
        String seconds = "" + date.getSeconds();

        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return hours + ":" + minutes + ":" + seconds;

    }

    public static int getWidestView(Context context, Adapter adapter) {
        int maxWidth = 0;
        View view = null;
        FrameLayout fakeParent = new FrameLayout(context);
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            view = adapter.getView(i, view, fakeParent);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = view.getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    public static int getHeight(Context context, Adapter adapter) {
        int height = 0;
        View view = null;
        FrameLayout fakeParent = new FrameLayout(context);
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            view = adapter.getView(i, view, fakeParent);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            height += view.getMeasuredHeight();
        }
        return height;
    }

    class BubbleOnClickListener implements View.OnLongClickListener {

        ChatMsg cM;

        private BubbleOnClickListener(ChatMsg cM) {
            this.cM = cM;
        }

        public boolean onLongClick(View arg0) {

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(cM.getText());

            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("text label", cM.getText());
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(mContext, "Copied message to Clipboard", Toast.LENGTH_SHORT).show();

            return true;

        }
    }

    class BubbleHeadOnClickListener implements View.OnLongClickListener {

        ChatMsg cM;
        ChatAdapter cA;

        private BubbleHeadOnClickListener(ChatMsg cM, ChatAdapter cA) {
            this.cM = cM;
            this.cA = cA;
        }

        public boolean onLongClick(View arg0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Name setzen fuer: " + cM.getIdentity());

//// Set up the input
            final EditText input = new EditText(mContext);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
            input.setHint("Name");
            input.setHintTextColor(Color.RED);

            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Test.localSettings.identity2Name.remove(cM.getIdentity());
                    Test.localSettings.identity2Name.put(cM.getIdentity(), input.getText().toString());
                    Test.localSettings.save();
                    cM.setName(input.getText().toString());
                    cA.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            return true;
        }
    }
}
