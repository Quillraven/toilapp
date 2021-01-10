import {makeStyles, Theme} from "@material-ui/core/styles";
import {createStyles} from "@material-ui/core";
import {GeoLocation} from "../model/GeoLocation";
import GoogleMapReact from "google-map-react"
import React from "react";
import {Room} from "@material-ui/icons";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        mapContainer: {
            marginLeft: theme.spacing(2),
            marginRight: theme.spacing(2),
            height: "25rem",
        },
        marker: {
            position: "absolute",
            transform: "translate(-50%, -100%)",
        },
        markerIcon: {
            fontSize: 64,
            color: theme.palette.primary.main,
        },
    })
)

interface GoogleMapsMarkerProps {
    // lat and lng attributes required for GoogleMapReact element
    lat: number
    lng: number
    label: string
    onMapClickCallback: () => void
}

function GoogleMapsMarker(props: GoogleMapsMarkerProps) {
    const classes = useStyles()

    return (
        <div className={classes.marker}>
            <Room className={classes.markerIcon} onClick={props.onMapClickCallback}/>
        </div>
    )
}

export interface GoogleMapsProps {
    markerName: string
    location: GeoLocation
    zoom: number
}

export default function GoogleMaps(props: GoogleMapsProps) {
    const classes = useStyles()

    const onMapClick = () => {
        const lon = props.location.x
        const lat = props.location.y
        window.open(`https://www.google.com/maps/place/${lat},${lon}/@${lat},${lon},${props.zoom}z`)
    }

    return (
        <div className={classes.mapContainer}>
            <GoogleMapReact
                bootstrapURLKeys={{key: process.env.REACT_APP_GOOGLE_MAPS_API_KEY}}
                center={{lat: props.location.y, lng: props.location.x}}
                zoom={props.zoom}
                onClick={onMapClick}
            >
                <GoogleMapsMarker
                    lat={props.location.y}
                    lng={props.location.x}
                    label={props.markerName}
                    onMapClickCallback={onMapClick}
                />
            </GoogleMapReact>
        </div>
    )
}
