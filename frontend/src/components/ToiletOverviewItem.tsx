import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Card, CardActionArea, CardContent, CardMedia, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import Box from "@material-ui/core/Box";
import {useHistory} from "react-router-dom"
import {ToiletOverview} from "../model/ToiletOverview";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            minWidth: 345,
        },
        media: {
            height: 140,
        },
    })
);

interface ToiletOverviewItemProps {
    toiletOverview: ToiletOverview
}

export default function ToiletOverviewItem(props: ToiletOverviewItemProps) {
    const classes = useStyles();
    const toiletOverview = props.toiletOverview;
    const history = useHistory()

    let distanceStr = toiletOverview.distance.toFixed(0) + "m";
    if (toiletOverview.distance >= 1000) {
        distanceStr = (toiletOverview.distance / 1000).toFixed(1) + "km";
    } else if (toiletOverview.distance < 0) {
        distanceStr = "-";
    }

    const handleCardClick = () => {
        history.push({
            pathname: `/${toiletOverview.id}`,
            state: {toiletId: toiletOverview.id}
        })
    };

    return (
        <Card className={classes.root}
              onClick={handleCardClick}
        >
            <CardActionArea>
                {
                    toiletOverview.previewURL &&
                    <CardMedia
                        className={classes.media}
                        image={toiletOverview.previewURL}
                        title={toiletOverview.title}
                    />
                }
                <CardContent>
                    <Typography gutterBottom variant="h5" component="h2">
                        <Box display="flex" flex="1" flexDirection="row" justifyContent="center">
                            {toiletOverview.title}
                            {<RatingView toiletId={toiletOverview.id} size="XS" rating={toiletOverview.rating}/>}
                        </Box>
                    </Typography>
                    <Typography variant="inherit">
                        Distance: {distanceStr}
                    </Typography>
                </CardContent>
            </CardActionArea>
        </Card>
    )
}
