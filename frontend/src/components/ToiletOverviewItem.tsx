import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Box, Card, CardActionArea, CardContent, CardMedia, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import {useHistory} from "react-router-dom"
import {ToiletOverview} from "../model/ToiletOverview";
import {AccessibleForward} from "@material-ui/icons";
import {getDistanceString} from "../services/ToiletService";

const useStyles = makeStyles((_: Theme) =>
    createStyles({
        card: {
            // display and width make the CardActionArea size equal to the grid item size
            display: "flex",
            flex: 1,
        },
        cardActionArea: {
            display: "flex",
            flex: 1,
            flexDirection: "column",
        },
        cardHeader: {
            display: "flex",
            flex: 1,
            width: "100%",
        },
        cardMedia: {
            display: "flex",
            flex: 1,
            width: "100%", // this is important; otherwise the image is not shown
            paddingTop: "56.25%", // 16:9
        },
        cardMediaApproval: {
            position: "absolute",
            width: "25%",
            height: "auto",
            top: 0,
            right: 0,
        },
        cardContent: {
            display: "flex",
            flex: 1,
            width: "100%",
            flexDirection: "column",
        },
        cardContentTitle: {
            display: "flex",
            flex: 1,
        },
        cardContentElse: {
            display: "flex",
            flex: 1,
        },
        ratingAndDistance: {
            flex: 1
        },
        disabledIcon: {
            alignSelf: "flex-end",
            fontSize: 40,
        },
    })
);

interface ToiletOverviewItemProps {
    toiletOverview: ToiletOverview
}

export default function ToiletOverviewItem(props: ToiletOverviewItemProps) {
    const approvalIconUrl = "/approval.png"
    const classes = useStyles();
    const toiletOverview = props.toiletOverview;
    const history = useHistory()

    const handleCardClick = () => {
        history.push({
            pathname: `/${toiletOverview.id}`,
            state: {toiletId: toiletOverview.id}
        })
    };

    const getToiletTitle = (title: string) => {
        // if title exceeds title content area then shorten it and display "..." at the end
        return title.length <= 44 ? title : (title.substr(0, 41) + "...")
    }

    return (
        <Card className={classes.card}
              onClick={handleCardClick}
        >
            <CardActionArea className={classes.cardActionArea}>
                <Box className={classes.cardHeader}>
                    {
                        toiletOverview.previewURL
                            // if image available -> show it
                            ? <CardMedia className={classes.cardMedia}
                                         image={toiletOverview.previewURL}
                                         title={toiletOverview.title}
                            />
                            // else -> add empty box to move content to bottom of card
                            : <Box className={classes.cardMedia}/>
                    }
                    {
                        toiletOverview.toiletCrewApproved &&
                        <img alt="approval" className={classes.cardMediaApproval} src={approvalIconUrl}/>
                    }
                </Box>
                <CardContent className={classes.cardContent}>
                    <Typography className={classes.cardContentTitle} gutterBottom variant="h5">
                        {getToiletTitle(toiletOverview.title)}
                    </Typography>
                    <Box className={classes.cardContentElse}>
                        <Box className={classes.ratingAndDistance}>
                            <RatingView toiletId={toiletOverview.id} size="S"
                                        rating={toiletOverview.rating}/>
                            <Typography>
                                Distance: {getDistanceString(toiletOverview.distance)}
                            </Typography>
                        </Box>
                        {toiletOverview.disabled && <AccessibleForward className={classes.disabledIcon}/>}
                    </Box>
                </CardContent>
            </CardActionArea>
        </Card>
    )
}
