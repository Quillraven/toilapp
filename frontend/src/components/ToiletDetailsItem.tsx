import {Box, createStyles, Divider, Grid, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import {ToiletDetails} from "../model/ToiletDetails";
import {getDistanceString} from "../services/ToiletService";
import {makeStyles, Theme} from "@material-ui/core/styles";
import {AccessibleForward} from "@material-ui/icons";
import Comments from "./Comments";
import UserRating from "./UserRating";
import axios from "axios";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        gridItem: {
            margin: theme.spacing(1),
        },
        image: {
            paddingTop: "6px",
            display: "block",
            marginLeft: "auto",
            marginRight: "auto",
            maxWidth: "100%",
            height: "auto",
            borderRadius: "42px",
        },
        title: {
            display: "flex",
            flex: 1,
        },
        toiletInfoBox: {
            display: "flex",
            flex: 1,
        },
        ratingAndDistance: {
            flex: 0
        },
        disabledIcon: {
            alignSelf: "flex-end",
            fontSize: 40,
            marginLeft: "42px",
        },
        dividerDescription: {
            marginTop: "12px",
            marginBottom: "12px",
        },
    })
)

interface ToiletDetailsItemProps {
    toiletDetails: ToiletDetails
}

export default function ToiletDetailsItem(props: ToiletDetailsItemProps) {
    const classes = useStyles();
    const toiletDetails = props.toiletDetails;

    return (
        <Grid
            container
            direction="row"
            justify="flex-start"
            alignItems="flex-start"
        >
            <Grid item xs={12} md={4} className={classes.gridItem}>
                {
                    toiletDetails.previewURL &&
                    <img className={classes.image} src={axios.defaults.baseURL + toiletDetails.previewURL}
                         alt={toiletDetails.title}/>
                }
                <Typography className={classes.title} gutterBottom variant="h3">
                    {toiletDetails.title}
                </Typography>
                <Box className={classes.toiletInfoBox}>
                    <Box className={classes.ratingAndDistance}>
                        <RatingView toiletId={toiletDetails.id} size="S"
                                    rating={toiletDetails.rating}/>
                        <Typography>
                            Distance: {getDistanceString(toiletDetails.distance)}
                        </Typography>
                    </Box>
                    {toiletDetails.disabled && <AccessibleForward className={classes.disabledIcon}/>}
                </Box>
                <Divider className={classes.dividerDescription} variant="fullWidth"/>
                <Typography>
                    {toiletDetails.description}
                </Typography>
                <Divider className={classes.dividerDescription} variant="fullWidth"/>
            </Grid>
            <Grid item xs={12} md={5} className={classes.gridItem}>
                <UserRating toiletId={toiletDetails.id}/>
                <Comments toiletDetails={toiletDetails}/>
            </Grid>
        </Grid>
    )
}
