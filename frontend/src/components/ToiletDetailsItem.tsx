import {Divider, Grid, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import Comments from "./Comments";
import {ToiletDetails} from "../model/ToiletDetails";

interface ToiletDetailsItemProps {
    toiletDetails: ToiletDetails
}

export default function ToiletDetailsItem(props: ToiletDetailsItemProps) {
    const toiletDetails = props.toiletDetails;

    let distanceStr = toiletDetails.distance.toFixed(0) + "m";
    if (toiletDetails.distance >= 1000) {
        distanceStr = (toiletDetails.distance / 1000).toFixed(1) + "km";
    } else if (toiletDetails.distance < 0) {
        distanceStr = "-";
    }

    return (
        <React.Fragment>
            <Grid container justify="flex-start">
                <Grid item
                      xs={12} sm={12} md={12} lg={6} xl={6}
                      style={{maxWidth: "100%"}}>
                    <img src={toiletDetails.previewURL} alt={toiletDetails.title} style={{width: "100%"}}/>
                </Grid>
                <Grid item
                      xs={12} sm={12} md={12} lg={6} xl={6}
                      style={{padding: "20px"}}>
                    <Typography gutterBottom variant="h3" component="h2" align="center">
                        {toiletDetails.title}
                    </Typography>
                    <RatingView toiletId={toiletDetails.id} size="S" rating={toiletDetails.rating}/>
                    <Typography variant="inherit">
                        Distance: {distanceStr}
                    </Typography>
                    <Typography>
                        {toiletDetails.description}
                    </Typography>
                    <br/>
                    <Divider variant="middle"/>
                    <Comments toiletDetails={toiletDetails}/>
                </Grid>
            </Grid>
        </React.Fragment>
    )
}
