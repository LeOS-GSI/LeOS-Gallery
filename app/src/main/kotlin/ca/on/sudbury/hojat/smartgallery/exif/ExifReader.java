package ca.on.sudbury.hojat.smartgallery.exif;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * This class reads the EXIF header of a JPEG file and stores it in
 * {@link }.
 */
class ExifReader {
    private static final String TAG = "ExifReader";

    private final ExifInterface mInterface;

    ExifReader(ExifInterface iRef) {
        mInterface = iRef;
    }

    /**
     * Parses the inputStream and and returns the EXIF data in an
     * {@link }.
     */
    protected ExifData read(InputStream inputStream, int options) throws ExifInvalidFormatException, IOException {
        ExifParser parser = ExifParser.parse(inputStream, options, mInterface);
        ExifData exifData = new ExifData(parser.getByteOrder());
        exifData.setSections(parser.getSections());
        exifData.mUncompressedDataPosition = parser.getUncompressedDataPosition();

        exifData.setQualityGuess(parser.getQualityGuess());
        exifData.setJpegProcess(parser.getJpegProcess());

        final int w = parser.getImageWidth();
        final int h = parser.getImageLength();

        if (w > 0 && h > 0) {
            exifData.setImageSize(w, h);
        }

        ExifTag tag;

        int event = parser.next();
        while (event != ExifParser.EVENT_END) {
            switch (event) {
                case ExifParser.EVENT_START_OF_IFD:
                    exifData.addIfdData(new IfdData(parser.getCurrentIfd()));
                    break;
                case ExifParser.EVENT_NEW_TAG:
                    tag = parser.getTag();


                    if (!tag.hasValue()) {
                        parser.registerForTagValue(tag);
                    } else {

                        if (parser.isDefinedTag(tag.getIfd(), tag.getTagId())) {
                            exifData.getIfdData(tag.getIfd()).setTag(tag);
                        } else {
                            Timber.w("skip tag because not registered in the tag table:%s", tag);
                        }
                    }
                    break;
                case ExifParser.EVENT_VALUE_OF_REGISTERED_TAG:
                    tag = parser.getTag();
                    if (tag.getDataType() == ExifTag.TYPE_UNDEFINED) {
                        parser.readFullTagValue(tag);
                    }
                    exifData.getIfdData(tag.getIfd()).setTag(tag);
                    break;
                case ExifParser.EVENT_COMPRESSED_IMAGE:
                    byte[] buf = new byte[parser.getCompressedImageSize()];
                    if (buf.length == parser.read(buf)) {
                        exifData.setCompressedThumbnail(buf);
                    } else {
                        Timber.w("Failed to read the compressed thumbnail");
                    }
                    break;
                case ExifParser.EVENT_UNCOMPRESSED_STRIP:
                    buf = new byte[parser.getStripSize()];
                    if (buf.length == parser.read(buf)) {
                        exifData.setStripBytes(parser.getStripIndex(), buf);
                    } else {
                        Timber.w("Failed to read the strip bytes");
                    }
                    break;
            }
            event = parser.next();
        }
        return exifData;
    }
}
