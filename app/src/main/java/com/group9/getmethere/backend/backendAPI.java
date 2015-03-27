package com.group9.getmethere.backend;

import android.content.Context;
import android.content.res.AssetManager;

import java.util.ArrayList;

public class backendAPI {

    public class Bus {
        String name;
        String from;
        String to;

        public Bus( String n, String f, String t ) {
            name = n; from = f; to = t;
        }
    }

    String serviceNames[] = { "54", "1", "15", "28A", "35", "54", "57", "58", "65", "M1", "M2" };

    private AssetManager assets;
    private tndsParse tnds = new tndsParse();
    private dataTimeDate tD = new dataTimeDate();

    public ArrayList <Bus> busses;

    // Constructor: parses the TNDS and stores the data
    public backendAPI(Context context) {
//ifdef android
	// Get assets handle
        assets = context.getAssets();
//endif android

        // Parse all known services
        for( int i = 0; i < serviceNames.length; i++ )
            tnds.parse( assets, serviceNames[ i ] + ".xml" );

        // Create an empty busses array
        busses = new ArrayList <Bus> ();

        // Fill it!
        for( int i = 0; i < serviceNames.length; i++ ) {
            dataService s = tnds.services.get( serviceNames[ i ] );
            Bus bus = new Bus( serviceNames[ i ], s.stdService.origin, s.stdService.destination );
            busses.add( bus );
        }
    }

    // Returns the current time as a string
    public String currentTime() {
        tD.setCurrent();
        return tD.hour() + ":" + tD.minute() + ":" + tD.second();
    }

    // Returns the current time as a dataTime object
    public dataTime dtCurrentTime() {
        tD.setCurrent();
        return new dataTime( tD.time() );
    }

    // Returns the total number of known services
    public int services() {
        return busses.size();
    }

    // Returns the name of service no. n from the busses array
    public String name( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.name;
        }

        return null;
    }

    // Returns the origin of service no. n from the busses array
    public String from( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.from;
        }

        return null;
    }

    // Returns the destination of service no. n from the busses array
    public String to( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.to;
        }

        return null;
    }

    // Tells you whether a given service has an active journey at the moment
    //  (i.e. we can / can't get data about it's current position and progress)
    public boolean isActive( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            if( service.activeJourney( tD, tD, false ) != service.NOT_FOUND )
                return true;
        }

        return false;
    }

    // Returns the name of the previous stop for service named <serviceName> (no live data, currently)
    public String previousStop( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                return tnds.stops.name( service.activeStopRefFrom( tD, tD, journey, false ) );
            }
        }

        return null;
    }

    // Returns the name of the next stop for service named <serviceName> (no live data, currently)
    public String nextStop( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                return tnds.stops.name( service.activeStopRefTo( tD, tD, journey, false ) );
            }
        }

        return null;
    }

    // Returns the progress between the two stops for service named <serviceName>, as a float between 0 and 1
    //  (no live data, currently)
    // Returns -1 if an error occurred
    public float progressBetweenStops( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                return service.activeLinkProgress( tD, tD, journey, false );
            }
        }

        return -1;
    }

    // Returns a String representation of the scheduled time of arrival at the next stop
    public String strScheduledTimeOfArrival( String serviceName ) {
        dataTime dT = scheduledTimeOfArrival( serviceName );
        if( dT != null )
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

        return null;
    }

    // Returns a String representation of the scheduled time of arrival at the next stop
    public String strTimeOfArrival( String serviceName ) {
        dataTime dT = timeOfArrival( serviceName );
        if( dT != null )
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

        return null;
    }

    // Returns a String representation of the delay of time of arrival to the next stop
    public String strTimeOfArrivalDelay( String serviceName ) {
        dataTime dT = timeOfArrivalDelay( serviceName );
        if( dT != null )
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

        return null;
    }

    // Returns the scheduled time of arrival at the next stop
    public dataTime scheduledTimeOfArrival( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                return new dataTime( service.scheduledTimeToStopRef( journey, service.activeStopRefTo( tD, tD, journey, false ) ) );
            }
        }

        return null;
    }

    // Returns the scheduled time of arrival at the next stop
    public dataTime timeOfArrival( String serviceName ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                tD.setCurrent();
                return new dataTime( service.timeToStopRef( tD, journey, service.activeStopRefTo( tD, tD, journey, false ), false ) );
            }
        }

        return null;
    }

    // Returns the delay of time of arrival to the next stop
    public dataTime timeOfArrivalDelay( String serviceName ) {
        dataTime scheduled = scheduledTimeOfArrival( serviceName );
        dataTime live      = timeOfArrival( serviceName );
        if( scheduled != null && live != null )
            return new dataTime( scheduled.time - live.time );

        return null;
    }
}