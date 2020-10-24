import {Grid, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import {Toilet} from "../model/Toilet";
import Comments from "./Comments";

interface ToiletDetailsItemProps {
    toilet: Toilet
}

export default function ToiletDetailsItem(props: ToiletDetailsItemProps) {
    const toilet = props.toilet;
 
    let distanceStr = toilet.distance.toFixed(0) + "m";
    if (toilet.distance >= 1000) {
        distanceStr = (toilet.distance / 1000).toFixed(1) + "km";
    } else if (toilet.distance < 0) {
        distanceStr = "-";
    }

    return (
        <React.Fragment>
            <Grid container justify="flex-start">
                <Grid item 
                    xs={12} sm={12} md={12} lg={6} xl={6}
                    style={{maxWidth: "100%"}}>
                    <img src={toilet.previewURL} alt={toilet.title} style={{width: "100%"}} />
                </Grid>
                <Grid item 
                    xs={12} sm={12} md={12} lg={6} xl={6}
                    style={{padding: "20px"}}>
                    <Typography gutterBottom variant="h3" component="h2" align="center">
                        {toilet.title}
                    </Typography>
                    <RatingView toiletId={toilet.id} size="S" rating={toilet.rating}/>
                    <Typography variant="inherit">
                        Distance: {distanceStr}
                    </Typography>
                    <Typography>
                        {toilet.description}
                    </Typography>
                </Grid>
            </Grid>
            <div>
                <Comments toilet={toilet}/>
            </div>
        </React.Fragment>
    )
}
