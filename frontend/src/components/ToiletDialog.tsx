import React, {Dispatch, forwardRef, SetStateAction, useImperativeHandle, useState} from "react";
import {
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    IconButton,
    TextField
} from "@material-ui/core";
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {PhotoCamera} from "@material-ui/icons";
import {GeoLocation} from "../model/GeoLocation";
import {ToiletServiceProvider} from "../services/ToiletService";

const useStyles = makeStyles((_: Theme) =>
    createStyles({
        inputImage: {
            display: "none",
        },
    }),
);

export interface ToiletDialogRef {
    handleOpen(): void
}

interface ToiletDialogProps {
    title: string,
    dispatchAlert: Dispatch<SetStateAction<boolean>>,
}

const EMPTY_LOCATION = 9999

export const ToiletDialog = forwardRef<ToiletDialogRef, ToiletDialogProps>((props, ref) => {
    const classes = useStyles()
    const toiletService = ToiletServiceProvider.getToiletService()
    const [open, setOpen] = useState(false)
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [thumbnail, setThumbnail] = useState<File | null>(null)
    const [disabled, setDisabled] = useState(false)
    // TODO prefill location with current location from user
    const [location, setLocation] = useState<GeoLocation>({lat: EMPTY_LOCATION, lon: EMPTY_LOCATION} as GeoLocation)

    const updateTitle = (e: React.ChangeEvent<HTMLInputElement>) => {
        setTitle(e.target.value)
    }

    const updateDescription = (e: React.ChangeEvent<HTMLInputElement>) => {
        setDescription(e.target.value)
    }

    const updateDisabled = (e: React.ChangeEvent<HTMLInputElement>) => {
        setDisabled(e.target.checked)
    }

    const updateThumbnail = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setThumbnail(e.target.files[0])
        } else {
            setThumbnail(null)
        }
    }

    const updateLongitude = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.value.length > 0) {
            setLocation(prevLocation => {
                return {...prevLocation, lon: e.target.valueAsNumber}
            })
        } else {
            setLocation(prevLocation => {
                return {...prevLocation, lon: EMPTY_LOCATION}
            })
        }
    }

    const updateLatitude = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.value.length > 0) {
            setLocation(prevLocation => {
                return {...prevLocation, lat: e.target.valueAsNumber}
            })
        } else {
            setLocation(prevLocation => {
                return {...prevLocation, lat: EMPTY_LOCATION}
            })
        }
    }

    function validateInput(): boolean {
        return title.length > 0 && location.lat !== EMPTY_LOCATION && location.lon !== EMPTY_LOCATION
    }

    const handleSubmit = () => {
        if (validateInput()) {
            toiletService
                .createUpdateToilet(
                    title,
                    location,
                    disabled,
                    description
                )
                .then(response => {
                        if (response) {
                            console.log(`Successfully created toilet '${response.id}'`)
                            if (thumbnail) {
                                toiletService
                                    .updatePreviewImage(response.id, thumbnail)
                                    .then(response => {
                                        if (response) {
                                            console.log(`Successfully updated preview`)
                                        }
                                    })
                            }
                            props.dispatchAlert(true)
                            handleClose()
                        }
                    }
                )
        }
    }

    const handleClose = () => {
        setTitle("")
        setDescription("")
        setThumbnail(null)
        setLocation({lat: EMPTY_LOCATION, lon: EMPTY_LOCATION})
        setOpen(false)
    }

    const handleOpen = () => {
        setOpen(true)
    }

    useImperativeHandle(ref, () => ({handleOpen}))

    return (
        <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">{props.title}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Provide the necessary information below and click on <b>OK</b> to submit it. <br/>
                    Fields marked with a <b>*</b> are mandatory.
                </DialogContentText>
                <TextField
                    required
                    error={title.length <= 0}
                    onChange={updateTitle}
                    autoFocus
                    margin="dense"
                    id="toilet-dialog-title"
                    label="Title"
                    fullWidth
                />
                <TextField
                    required
                    onChange={updateLongitude}
                    error={location.lon === EMPTY_LOCATION}
                    margin="dense"
                    id="toilet-dialog-longitude"
                    label="Longitude"
                    type="number"
                />
                <TextField
                    required
                    onChange={updateLatitude}
                    error={location.lat === EMPTY_LOCATION}
                    margin="dense"
                    id="toilet-dialog-latitude"
                    label="Latitude"
                    type="number"
                />
                <br/>
                <Box display="flex" alignItems="center">
                    <TextField
                        margin="dense"
                        label="Disabled"
                        disabled
                    />
                    <Checkbox
                        color="primary"
                        onChange={updateDisabled}
                    />
                </Box>
                <TextField
                    margin="dense"
                    onChange={updateDescription}
                    id="toilet-dialog-description"
                    label="Description"
                    fullWidth
                />
                <input accept="image/png,image/jpeg"
                       className={classes.inputImage}
                       id="toilet-dialog-image"
                       type="file"
                       onChange={updateThumbnail}
                />
                <label htmlFor="toilet-dialog-image">
                    <Box display="flex" alignItems="center">
                        <TextField
                            margin="dense"
                            label={thumbnail ? thumbnail.name.substr(0, 15) : "Thumbnail"}
                            disabled
                        />
                        <IconButton color="primary" aria-label="upload picture" component="span">
                            <PhotoCamera/>
                        </IconButton>
                    </Box>
                </label>
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleSubmit} color="primary">
                    OK
                </Button>
            </DialogActions>
        </Dialog>
    )
})
