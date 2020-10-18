import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Card, CardActionArea, CardContent, CardMedia, Typography} from "@material-ui/core";
import React from "react";
import {RatingView} from "./Rating";
import {Toilet} from "../model/Toilet";
import Box from "@material-ui/core/Box";
import {useHistory} from "react-router-dom"

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
    toilet: Toilet
}

export default function ToiletOverviewItem(props: ToiletOverviewItemProps) {
    const classes = useStyles();
    const toilet = props.toilet;
    const history = useHistory()

    let distanceStr = toilet.distance.toFixed(0) + "m";
    if (toilet.distance >= 1000) {
        distanceStr = (toilet.distance / 1000).toFixed(1) + "km";
    } else if (toilet.distance < 0) {
        distanceStr = "-";
    }

    const handleCardClick = () => {
        history.push({
            pathname: `/${toilet.id}`,
            state: {toilet: toilet}
        })
    };

    return (
        <Card className={classes.root}
              onClick={handleCardClick}
        >
            <CardActionArea>
                {
                    toilet.previewURL &&
                    <CardMedia
                        className={classes.media}
                        image={toilet.previewURL}
                        title={toilet.title}
                    />
                }
                <CardContent>
                    <Typography gutterBottom variant="h5" component="h2">
                        <Box display="flex" flex="1" flexDirection="row" justifyContent="center">
                            {toilet.title}
                            {<RatingView toiletId={toilet.id} size="XS" rating={toilet.rating}/>}
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
