package gov.nasa.jpl.mbee.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;

/**
 * @author bclement
 *
 */
public class TimeUtils {

    public static enum Units { 
        days(24*3600*1e9), hours(3600*1e9), minutes(60*1e9),
        seconds(1e9), milliseconds(1e6), microseconds(1e3),
        nanoseconds(1);
        
        private double factor;
        
        Units(double f) {
          factor = f;
        }
        
        public static double conversionFactor( Units fromUnits, Units toUnits) {
          double f = ((double)fromUnits.factor)/toUnits.factor;
    //      if ( Debug.isOn() ) Debug.outln( "conversionFactor(" + fromUnits + ", " + toUnits
    //                          + ") = " + fromUnits.factor + " / " + toUnits.factor
    //                          + " = " + f );
          return f;
        }
        public String toShortString() {
          switch (this) {
            case days:
              return "d";
            case hours:
              return "h";
            case minutes:
              return "m";
            case seconds:
              return "s";
            case milliseconds:
              return "ms";
            case microseconds:
              return "\u00B5s";
            case nanoseconds:
              return "ns";
            default:
              return null;
          }
        }
    
        public static Units fromString( String unitsString ) {
          Units unit = null;
          try {
            if ( unitsString == null || unitsString.length() == 0 ) {
              Assert.fail( "Parse of units from \"" + unitsString + "\" failed!" );
            }
            if ( unitsString.equals( microseconds.toShortString() ) ) {
              unit = microseconds;
            } else {
              switch ( unitsString.charAt( 0 ) ) {
                case 'd':
                  unit = days;
                  break;
                case 'h':
                  unit = hours;
                  break;
                case 's':
                  unit = seconds;
                  break;
                case 'n':
                  unit = nanoseconds;
                  break;
                case 'm':
                  if ( unitsString.length() == 1 ) {
                    unit = minutes;
                    break;
                  } else {
                    switch ( unitsString.charAt( 1 ) ) {
                      case 'i':
                        if ( unitsString.length() <= 2 ) {
                          Assert.fail( "Parse of units from \"" + unitsString
                                       + "\" failed!" );
                        } else {
                          switch ( unitsString.charAt( 2 ) ) {
                            case 'n':
                              unit = minutes;
                              break;
                            case 'l':
                              unit = milliseconds;
                              break;
                            case 'c':
                              unit = microseconds;
                              break;
                            default:
                              Assert.fail( "Parse of units from \"" + unitsString
                                           + "\" failed!" );
                          }
                        }
                        break;
                      case 's':
                        unit = milliseconds;
                        break;
                      default:
                        Assert.fail( "Parse of units from \"" + unitsString
                                     + "\" failed!" );
                    }
                  }
                  break;
                default:
                  Assert.fail( "Parse of units from \"" + unitsString
                               + "\" failed!" );
              }
            }
            if ( unit != null && !unitsString.equals( unit.toString() )
                 && !unitsString.equals( unit.toShortString() ) ) {
              Assert.fail( "Parse of units from \"" + unitsString + "\" failed!" );
            }
          } catch ( Exception e ) {
            e.printStackTrace();
          }
          return unit;
        }
      }

    public static final double Julian_Jan_1_2000 = 2451544.500000;
    public static final Date Date_Jan_1_1970 = new Date(0); //Calendar.set(year + 1900, month, date)
    public static final Calendar gmtCal =
            Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
    protected static final Calendar cal_Jan_1_2000 = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ) ) {
        private static final long serialVersionUID = 1L;
        {
            clear();
            setTimeZone( TimeZone.getTimeZone( "GMT" ) );
            set( 2000, Calendar.JANUARY, 1 );
        }
    };
    protected static final long millis_Jan_1_2000 = cal_Jan_1_2000.getTimeInMillis();

    public static final String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String fileTimestampFormat = timestampFormat;
    public static final String dayOfYearTimestampFormat = "yyyy-DDD'T'HH:mm:ss.SSSZ";
    public static final String aspenTeeFormat = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String formatsToTry[] =
            // WARNING! timestampHasTimezone() is written to take advantage of this
            // restricted set, so it may need to be modified if formatToTry is modified.
            { TimeUtils.timestampFormat,
              TimeUtils.timestampFormat.replace( ".SSS", "" ),
              TimeUtils.timestampFormat.replace( "Z", "" ),
              TimeUtils.timestampFormat.replace( ".SSSZ", "" ),
              TimeUtils.dayOfYearTimestampFormat, 
              TimeUtils.dayOfYearTimestampFormat.replace( ".SSS", "" ),
              TimeUtils.dayOfYearTimestampFormat.replace( "Z", "" ),
              TimeUtils.dayOfYearTimestampFormat.replace( ".SSSZ", "" ),
              "EEE MMM dd HH:mm:ss zzz yyyy" };
    
    /**
     * MaxDate is roughly the year 3000
     */
    private static final Date MaxDate = new Date((long)1030 * 365 * 24 * 3600 * 1000);
    static long f = Long.MAX_VALUE;
    public static Boolean allFormatsHaveColon = null;
    public static boolean allFormatsHaveColon() {
        if ( allFormatsHaveColon == null ) {
            allFormatsHaveColon = true;
            for ( int i = 0; i < formatsToTry.length; ++i ) {
                String format = formatsToTry[i];
                if ( !format.contains( ":" ) ) {
                    allFormatsHaveColon = false;
                    break;
                }
            }
        }
        return allFormatsHaveColon;
    }

    protected static int lastFormat = 0;
    protected static synchronized int getLastFormat() {
        return lastFormat;
    }
    protected static synchronized void setLastFormat(int i) {
        lastFormat = i;
    }
    protected static int lastTimestampLength = 0;
    protected static synchronized int getLastTimestampLength() {
        return lastTimestampLength;
    }
    protected static synchronized void setLastTimestampLength(int i) {
        lastTimestampLength = i;
    }
    
    /**
     * Determine whether the input timestamp specifies milliseconds, for
     * example, like "333" in "2021-01-01T01:01.333."
     * 
     * @param timestamp
     * @return
     */
    public static boolean timestampHasMilliseconds( String timestamp ) {
        int posPeriod = timestamp.lastIndexOf( '.' );
        if ( posPeriod < 0 ) return false;
        int posColon = timestamp.lastIndexOf( ':' );
        if ( posColon < posPeriod ) return true;
        return false;
    }
    
    /**
     * Determine whether the input timestamp have a timezone of the style -0700 or +0300.
     * @param timestamp
     * @return
     */
    public static boolean timestampHasTimezone( String timestamp ) {
        // We could simply return timestamp.matches("
        // [+-][0-2]?[0-9][0-9][0-9]") but we want to be faster than a regex
        // match.
        // WARNING! The implementation assumes that the format is one of those
        // in TimeUtils.formatsToTry().
        int length = timestamp.length();
        int posColon = timestamp.lastIndexOf( ':' );
        // ex. 2020-04-04T12:34:56.789-0700
        int colonCharsFromEnd = length - posColon - 1;
        if ( colonCharsFromEnd < 4 ) return false;
        if ( colonCharsFromEnd > 6 ) {
            // For xml format, "EEE MMM dd HH:mm:ss zzz yyyy", we want to return
            // false since we're looking for a -0700 style timezone.
            if ( length > 9 && timestamp.substring( length - 9 ).matches( ".* [A-Z][A-Z][A-Z].*" ) ) return false;
            return true;
        }
        int posMinus = timestamp.lastIndexOf( '-' );
        int minusCharsFromEnd = length - posMinus - 1;
        if ( minusCharsFromEnd < colonCharsFromEnd && minusCharsFromEnd > 5 ) return true;
//        boolean hasMillis = timestampHasMilliseconds( timestamp );
//        if ( hasMillis ) {
//            int posPeriod = timestamp.lastIndexOf( '.' );
//            posPeriod < 
//        }
        int posPlus = timestamp.lastIndexOf( '+' );
        int plusCharsFromEnd = length - posPlus - 1;
        if ( plusCharsFromEnd < colonCharsFromEnd && plusCharsFromEnd < 5 ) return true;

        return false;
    }
    protected static boolean timestampFormatHasMilliseconds( String timestamp ) {
        if ( timestamp.contains( ".SSS" ) ) return true;
        return false;
    }
    protected static boolean timestampFormatHasTimezone( String timestamp ) {
        if ( timestamp.endsWith( "Z" ) ) return true;
        return false;
    }
    
    /**
     * Parse the specified timestamp String in tee format and return the
     * corresponding Date.
     * 
     * @param timestamp
     *            the time in tee format (yyyy-MM-dd'T'HH:mm:ss.SSSZ,
     *            yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.SSS,
     *            yyyy-MM-dd'T'HH:mm:ss, yyyy-DDD'T'HH:mm:ss or variants similar
     *            to previous, or EEE MMM dd HH:mm:ss zzz yyyy)
     * @return the Date for the timestamp or null if the timestamp format is not
     *         recognized.
     */
    public static Date dateFromTimestamp( String timestamp, TimeZone defaultTimeZone ) {
        if ( Utils.isNullOrEmpty( timestamp ) ) return null;
        
        int pos = timestamp.lastIndexOf( ':' );

        try {
            if ( pos == -1 ) {
                Double jd = Double.parseDouble( timestamp );
                if ( jd != null ) {
                    Date d = julianToDate( jd );
                    if ( d != null && d.after( TimeUtils.Date_Jan_1_1970 ) &&
                        d.before( TimeUtils.MaxDate ) ) {
                        return d;
                    }
                }
            }
        } catch (NumberFormatException e) {}
        
        // If all formats have a colon, then go ahead and return null if no
        // colon was found.
        if ( pos == -1  && allFormatsHaveColon() ) {
            return null;
        }
        // This converts an xml time zone of format -07:00 to -0700. If the last
        // colon is three characters from the end, and the timestamp has three
        // colons, then remove the last colon.
        if ( pos == timestamp.length() - 3
             && timestamp.replaceAll( "[^:]", "" ).length() == 3 ) {
          timestamp = timestamp.replaceFirst( ":([0-9][0-9])$", "$1" );
        }
        
        // A format without a time zone will match a string that has a time
        // zone, so we need to make sure the format includes a time zone and
        // milliseconds if the date string does.
        boolean stringHasTimezone = timestampHasTimezone( timestamp ); 
        boolean stringHasMillis = timestampHasMilliseconds( timestamp );
        int timestampLength = timestamp.length();

        
        int i = getLastFormat();
        int lastLength = getLastTimestampLength();
        for ( int j = 0; j < formatsToTry.length; ++j ) {
          // use the last format for j == 0; then skip the last format if it is not 0.
          if ( j > 0 ) {
              if ( j <= i ) i = j - 1;
              else i = j;
          }
          String format = formatsToTry[i];
          
          // Check to see if format has timezone and millis as appropriate. 
          if ( i != getLastFormat() || lastLength != timestampLength  ) {
              if ( stringHasTimezone != timestampFormatHasTimezone( format ) ) continue;
              if ( stringHasMillis != timestampFormatHasMilliseconds( format ) ) continue;
          }
          
          DateFormat df = new SimpleDateFormat( format );
          df.setCalendar( TimeUtils.gmtCal );
          if ( defaultTimeZone != null ) {
              df.setTimeZone( defaultTimeZone );
          } else {
              df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
          }
          try {
            Date d = df.parse( timestamp );
            setLastFormat( i );
            setLastTimestampLength( timestampLength );
            return d;
          } catch ( IllegalArgumentException e1 ) {
            if ( j == formatsToTry.length - 1 ) {
              e1.printStackTrace();
            }
          } catch ( ParseException e ) {
            if ( j == formatsToTry.length - 1 ) {
              e.printStackTrace();
            }
          }
        }
        return null;
    }

    public static long fromTimestampToMillis( String timestamp ) {
      long t = 0;
      DateFormat df = new SimpleDateFormat( timestampFormat );
      try {
        Date d = df.parse( timestamp );
        assert ( d != null );
        t = d.getTime();
      } catch ( java.text.ParseException e1 ) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      return t;
    }

    // Converts time offset to a date-time String in Timepoint.timestamp format.
    // Assumes t is an offset from Timepoint.epoch in Timepoint.units. 
    public static String timestampForFile() {
      String timeString =
          new SimpleDateFormat( fileTimestampFormat ).format( System.currentTimeMillis() );
      return timeString;
    }

    /**
     * Converts time in milliseconds since the "epoch" to a date-time String in
     * {@link #timestampFormat}.
     * 
     * @param millis
     *            milliseconds since Jan 1, 1970
     * @return a timestamp String
     */
    public static String toTimestamp( long millis ) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis( millis );
      String timeString =
          new SimpleDateFormat( timestampFormat ).format( cal.getTime() );
      return timeString;
    }
    
    public static String toTimestamp( Date dateTime ) {
        return toTimestamp( dateTime.getTime() );
    }

    public static String toAspenTimeString( long millis ) {
      return toAspenTimeString( millis, aspenTeeFormat );
    }

    public static String toAspenTimeString( Date d ) {
      return toAspenTimeString( d, aspenTeeFormat );
    }

    public static String toAspenTimeString(Date d, String format) {
      if (d != null) {
        return toAspenTimeString(d.getTime(), format);
      } else {
          if ( Debug.isOn() ) Debug.errln("Cannot convert null Date");
        return null;
      }
    }

    public static String toAspenTimeString(long millis, String format) {
      if (format == null)
        return null;
      Calendar cal = Calendar.getInstance();
      cal.setTimeZone(TimeZone.getTimeZone("GMT"));
      cal.setTimeInMillis(millis);
      String timeString = new SimpleDateFormat(format).format(cal.getTime());
      return timeString;    
    }

    /**
     * Return a {@link java.util.Date} for the Julian date number.
     * @param julianDateNumber
     * @return
     */
    public static Date julianToDate( Double julianDateNumber ) {
        if ( julianDateNumber == null ) return null;
        long millis = julianToMillis( julianDateNumber );
        Date date = new Date(millis);
        return date;
    }
    
    /**
     * Return the number of milliseconds since January 1, 1970 for the Julian date number. 
     * @param julianDate
     * @return milliseconds
     */
    public static long julianToMillis( Double julianDate ) {
        double deltaDays = julianDate - Julian_Jan_1_2000;
        double deltaMillis = deltaDays * 24 * 3600 * 1000;
        long millis = millis_Jan_1_2000 + (long)deltaMillis;
        return millis;
    }

//    // When compiling with Java 1.8
//    public static String toDurationStringHHMMSS(Duration dur) {
//        return nanosToDurationStringHHMMSS(dur.toNanos());
//    }
    public static String nanosToDurationStringHHMMSS(long nanos) {
        long n = nanos;
        long m = n / 1000000;
        long s = m / 1000;
        long d = s / (24 * 3600);
//        if ( d > 365 ) {
//            Debug.error(true, false, "WARNING! Duration nanos is a year or bigger: " + nanos);
//        }
        String hhmmss = String.format("%02d:%02d:%02d", (s / 3600) % 24, (s % 3600) / 60, (s % 60));
        String ms = ( ( m % 1000 == 0 ) ? "" :  String.format(".%03d", m % 1000) );
        String ds = ( (d == 0L) ? "" : String.format("%03dT", d) );
        String dStr = ds + hhmmss + ms;
        return dStr;

    }

    public static Double toDurationInSeconds( String field ) {
        Double d = null;
        // WARNING! -- this assumes that the number is in seconds!
        try {
            d = Double.parseDouble( field );
        } catch (NumberFormatException e) {}
        if ( d != null ) return d;
        
        // Try regex
        Matcher m = null;
        if ( field.contains( ":" ) ) {
            Pattern p = Pattern.compile( "\\s*((\\d+):)?(\\d+):(\\d\\d?)([.](\\d+))?\\s*" );
            m = p.matcher( field );
            long hrs = 0;
            long mins = 0;
            long secs = 0;
            long millis = 0;
            if ( !m.matches() ) {
                System.out.println( "no match!" );
            } else {
                System.out.println( "matches!" );
                for ( int i = 1; i < m.groupCount() + 1; ++i ) {
                    System.out.println( i + " " + m.group( i ) );
                }
                try {
                    if ( m.groupCount() == 2 ) {
                        if ( !Utils.isNullOrEmpty( m.group( 1 ) ) ) {
                            mins = Integer.parseInt( m.group( 1 ) );
                        }
                        if ( !Utils.isNullOrEmpty( m.group( 2 ) ) ) {
                            secs = Integer.parseInt( m.group( 2 ) );
                        }
                    } else if ( m.groupCount() == 4 ) {
                        if ( m.group(3).contains(".") ) {
                            if ( !Utils.isNullOrEmpty( m.group( 1 ) ) ) {
                                mins = Integer.parseInt( m.group( 1 ) );
                            }
                            if ( !Utils.isNullOrEmpty( m.group( 2 ) ) ) {
                                secs = Integer.parseInt( m.group( 2 ) );
                            }
                            if ( !Utils.isNullOrEmpty( m.group( 4 ) ) ) {
                                millis = Integer.parseInt( m.group( 4 ) );
                            }
                        } else {
                            if ( !Utils.isNullOrEmpty( m.group( 2 ) ) ) {
                                hrs = Integer.parseInt( m.group( 2 ) );
                            }
                            if ( !Utils.isNullOrEmpty( m.group( 3 ) ) ) {
                                mins = Integer.parseInt( m.group( 3 ) );
                            }
                            if ( !Utils.isNullOrEmpty( m.group( 4 ) ) ) {
                                secs = Integer.parseInt( m.group( 4 ) );
                            }
                        }
                    } else if ( m.groupCount() == 6 ) {
                        if ( !Utils.isNullOrEmpty( m.group( 2 ) ) ) {
                            hrs = Integer.parseInt( m.group( 2 ) );
                        }
                        if ( !Utils.isNullOrEmpty( m.group( 3 ) ) ) {
                            mins = Integer.parseInt( m.group( 3 ) );
                        }
                        if ( !Utils.isNullOrEmpty( m.group( 4 ) ) ) {
                            secs = Integer.parseInt( m.group( 4 ) );
                        }
                        if ( !Utils.isNullOrEmpty( m.group( 6 ) ) ) {
                            millis = Integer.parseInt( m.group( 6 ) );
                        }
                    }

                    double durationInMillis = ((hrs * 60 + mins) * 60 + secs) +  millis / 1000.0; 
                    return durationInMillis;
                    
                } catch (NumberFormatException e) {}
            }
        }
        
        // try xml duration
        //long secs = XmlUtils.getDurationInSeconds( field ) ;

        
        return null;
    }
    
    public static void main( String[] args ) {
        String s = " 01:01:01";
        Double d = toDurationInSeconds( s );
        System.out.println( "duration of " + s + " in millis = " + d );
        s = "24:00:00.777 ";
        d = toDurationInSeconds( s );
        System.out.println( "duration of " + s + " in millis = " + d );
    }
}
