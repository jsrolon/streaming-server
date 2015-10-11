package server;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class Main extends MediaListenerAdapter {

	/**
	 * The number of seconds between frames.
	 */

	public static final double SECONDS_BETWEEN_FRAMES = 1/4;

	/**
	 * The number of micro-seconds between frames.
	 */

	public static final long MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND
			* SECONDS_BETWEEN_FRAMES);

	/** Time of last frame write. */

	private static long mLastPtsWrite = Global.NO_PTS;

	/**
	 * The video stream index, used to ensure we display frames from one and
	 * only one video stream from the media container.
	 */

	private int mVideoStreamIndex = -1;

	private JFrame jf;

	private JPanel jp;

	private JLabel jl;

	public Main() {
		jf = new JFrame();
		jf.setSize(800, 600);
		jp = new JPanel();
		jl = new JLabel();
		jp.add(jl);
		jf.add(jp);
		jf.setVisible(true);

		// create a media reader for processing video

		IMediaReader reader = ToolFactory.makeReader("/Users/jsrolon/Desktop/sample.mp4");

		// stipulate that we want BufferedImages created in BGR 24bit color
		// space
		reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

		// note that DecodeAndCaptureFrames is derived from
		// MediaReader.ListenerAdapter and thus may be added as a listener
		// to the MediaReader. DecodeAndCaptureFrames implements
		// onVideoPicture().
		reader.addListener(this);

		// read out the contents of the media file, note that nothing else
		// happens here. action happens in the onVideoPicture() method
		// which is called when complete video pictures are extracted from
		// the media source
		while (reader.readPacket() == null)
			do {
			} while (false);
	}

	public void onVideoPicture(IVideoPictureEvent event) {
		try {
			// if the stream index does not match the selected stream index,
			// then have a closer look
			if (event.getStreamIndex() != mVideoStreamIndex) {
				// if the selected video stream id is not yet set, go ahead an
				// select this lucky video stream
				if (-1 == mVideoStreamIndex)
					mVideoStreamIndex = event.getStreamIndex();

				// otherwise return, no need to show frames from this video
				// stream
				else
					return;
			}

			// if uninitialized, backdate mLastPtsWrite so we get the very
			// first frame
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

			// if it's time to write the next frame
			if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {
				jl.setIcon(new ImageIcon(event.getImage()));

				// update last write time
				mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}

}
