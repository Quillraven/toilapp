import {Box, createStyles, Snackbar, Typography} from "@material-ui/core";
import {makeStyles, Theme} from "@material-ui/core/styles";
import {RatingSelect} from "./Rating";
import {RatingServiceProvider} from "../services/RatingService";
import React, {useEffect, useState} from "react";
import {Alert} from "@material-ui/lab";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        userRatingBox: {
            display: "flex",
            flexDirection: "row",
            justifyContent: "flex-start",
            alignItems: "center",
            alignContent: "flex-start",
        },
        userRatingText: {
            marginRight: theme.spacing(4),
        },
    })
)

interface UserRatingRef {
    ratingId: string,
    rating: number,
}

interface UserRatingProps {
    toiletId: string
}

export default function UserRating(props: UserRatingProps) {
    const classes = useStyles()
    const ratingService = RatingServiceProvider.getRatingService()
    const [ratingRef, setRatingRef] = useState({ratingId: "", rating: 0} as UserRatingRef)
    const [showAlert, setShowAlert] = useState(false)

    const ratingChanged = async (newValue: number) => {
        const newRating = await ratingService.createUpdateRating(props.toiletId, newValue, ratingRef.ratingId)
        console.log(`Updated user rating to value '${newRating.value}'`)
        setRatingRef({ratingId: newRating.id, rating: newRating.value})
        setShowAlert(true)
    }

    const closeAlert = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }

        setShowAlert(false)
    }

    useEffect(() => {
        if (props.toiletId) {
            (async () => {
                const currUserRating = await ratingService.getUserRating(props.toiletId)
                if (currUserRating) {
                    console.log(`Found user rating of value '${currUserRating.value}'`)
                    setRatingRef({ratingId: currUserRating.id, rating: currUserRating.value})
                } else {
                    console.log(`There is no user rating for toilet '${props.toiletId}'`)
                }
            })()
        }
    }, [props.toiletId, ratingService])

    return (
        <Box className={classes.userRatingBox}>
            <Snackbar open={showAlert} autoHideDuration={6000} onClose={closeAlert}>
                <Alert severity="success" onClose={closeAlert}>
                    Successfully updated rating
                </Alert>
            </Snackbar>
            <Typography className={classes.userRatingText} variant="h3">
                Your rating:
            </Typography>
            <RatingSelect toiletId={props.toiletId}
                          onRatingChange={ratingChanged}
                          rating={ratingRef.rating}
                          size={"M"}
            />
        </Box>
    )
}