clear
close all

D=2;

load J.txt
load RobotsPositions.txt
load pointsToMonitor.txt
%load Parameters.properties

RobotsPositions = RobotsDecisions;

nr = size(RobotsPositions,2)/D;
Tmax = size(J,1);
cmapRobot = hsv(nr);

n_p = size(pointsToMonitor,2)/D;

n_pointsToMonitor = zeros(n_p,2);
k1=1;
k2=1;
for i=1:n_p*D
    if mod(i,2)==0
        n_pointsToMonitor(k1,2) = pointsToMonitor(1,i);
        k1=k1+1;
    else
        n_pointsToMonitor(k2,1) = pointsToMonitor(1,i);
        k2=k2+1;
    end
end
pointsToMonitor = n_pointsToMonitor;

xmin = min(pointsToMonitor(:,1));
xmax = max(pointsToMonitor(:,1));
ymin = min(pointsToMonitor(:,2));
ymax = max(pointsToMonitor(:,2));

figureHandle= figure;

figure(figureHandle);

for t=2:Tmax
    clf(figureHandle,'reset')
    title('Robots Trajectories over time')
    hold on
    plot([0,1]',[0,1]','w');
    plot(pointsToMonitor(:,1),pointsToMonitor(:,2),'--','LineWidth',2);
    for r=1:nr
        plot(RobotsPositions(2:t,2*(r-1)+1),RobotsPositions(2:t,2*(r-1)+2),'Color',cmapRobot(r,:))
        scatter(RobotsPositions(t,2*(r-1)+1),RobotsPositions(t,2*(r-1)+2),...
            70,'filled','MarkerFaceColor',cmapRobot(r,:))
        %geoshow(RobotsPositions(t-1:t,2*(r-1)+2),RobotsPositions(t-1:t,2*(r-1)+1))
    end
    pause(0.002)
end
%title('Robots Trajectories in Latitude/Longitude format')
hold off

figure,
title('Final configuration')
hold on
plot([0,1],[0,0],'w');
plot(pointsToMonitor(:,1),pointsToMonitor(:,2),'--','LineWidth',2);
 for r=1:nr
        scatter(RobotsPositions(Tmax,2*(r-1)+1),RobotsPositions(Tmax-1,2*(r-1)+2),...
            70,'filled','MarkerFaceColor',cmapRobot(r,:))
 end
hold off

figure,
plot(J)
title('Cost function evolution');