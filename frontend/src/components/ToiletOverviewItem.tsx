import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Box, Card, CardActionArea, CardContent, CardMedia, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import {useHistory} from "react-router-dom"
import {ToiletOverview} from "../model/ToiletOverview";

const useStyles = makeStyles((_: Theme) =>
    createStyles({
        card: {
            // display and width make the CardActionArea size equal to the grid item size
            display: "flex",
            width: "100%",
        },
        cardMedia: {
            paddingTop: "56.25%", // 16:9
        },
        cardContent: {
            flex: 1,
            height: "135px"
        },
        cardContentTitle: {
            height: "55px",
        }
    })
);

interface ToiletOverviewItemProps {
    toiletOverview: ToiletOverview
}

export default function ToiletOverviewItem(props: ToiletOverviewItemProps) {
    const classes = useStyles();
    const toiletOverview = props.toiletOverview;
    const history = useHistory()

    const handleCardClick = () => {
        history.push({
            pathname: `/${toiletOverview.id}`,
            state: {toiletId: toiletOverview.id}
        })
    };

    const getDistance = (distance: number) => {
        if (distance >= 1000) {
            return (distance / 1000).toFixed(1) + "km";
        } else if (distance < 0) {
            return "-";
        } else {
            return distance.toFixed(0) + "m";
        }
    }

    const getToiletTitle = (title: string) => {
        // if title exceeds title content area then shorten it and display "..." at the end
        return title.length <= 44 ? title : (title.substr(0, 41) + "...")
    }

    return (
        <Card className={classes.card}
              onClick={handleCardClick}
        >
            <CardActionArea>
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
                <CardContent className={classes.cardContent}>
                    <Typography className={classes.cardContentTitle} gutterBottom variant="h5">
                        {getToiletTitle(toiletOverview.title)}
                    </Typography>
                    <RatingView toiletId={toiletOverview.id} size="S"
                                rating={toiletOverview.rating}/>
                    <Typography>
                        Distance: {getDistance(toiletOverview.distance)}
                    </Typography>
                </CardContent>
            </CardActionArea>
        </Card>
    )
}
